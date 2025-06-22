package io.github.aalopatin.spark.connector.datafaker

import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._
import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig, PartitionConfig}
import io.github.aalopatin.spark.connector.datafaker.generation.{DataGenerator, Types}
import org.apache.spark.sql.connector.catalog.{Table, TableProvider}
import org.apache.spark.sql.connector.expressions.Transform
import org.apache.spark.sql.sources.DataSourceRegister
import org.apache.spark.sql.types._
import org.apache.spark.sql.util.CaseInsensitiveStringMap
import org.slf4j.LoggerFactory

import java.util
import scala.jdk.CollectionConverters._

class DatafakerTableProvider extends TableProvider with DataSourceRegister {

  private val logger = LoggerFactory.getLogger(this.getClass)

  override def shortName(): String = "datafaker"

  override def inferSchema(options: CaseInsensitiveStringMap): StructType = {

    if (options.containsKey("schema")) {
      val schemaString = options.get("schema")
      DataType.fromJson(schemaString).asInstanceOf[StructType]
    } else {
      StructType(
        options
          .asScala
          .filter({ case (key, _) => key.startsWith("fields.")})
          .map({case (key, _) => StructField(key.stripPrefix("fields."), StringType)})
          .toArray
      )
    }
  }

  override def getTable(schema: StructType, partitioning: Array[Transform], properties: util.Map[String, String]): Table = {
    implicit val customConfig: Configuration = Configuration.default.withDefaults
    implicit val configDecoder: Decoder[Config] = deriveConfiguredDecoder[Config]
    val config = parseConfig[Config](properties)

    implicit val partitionConfigDecoder: Decoder[PartitionConfig] = deriveConfiguredDecoder[PartitionConfig]
    val partitionConfig = parseConfig[PartitionConfig](properties)

    val fields = parseFields(properties)
    val validatedSchema = validateSchema(schema, config)
    new DatafakerTable(validatedSchema, config, partitionConfig, fields)
  }


  private def validateSchema(schema: StructType, config: Config, fieldsPrefix: String = ""): StructType = {
    def validateDataType(dataType: DataType, config: Config, fieldPrefix: String): DataType = {
      dataType match {
        case structType: StructType =>
          validateSchema(structType, config, fieldPrefix)
        case ArrayType(elementType, containsNull) =>
          ArrayType(validateDataType(elementType, config, fieldPrefix), containsNull)
        case MapType(keyType, valueType, valueContainsNull) =>
          MapType(
            validateDataType(keyType, config, DataGenerator.fieldName("key", fieldPrefix)),
            validateDataType(valueType, config, DataGenerator.fieldName("value", fieldPrefix)),
            valueContainsNull
          )
        case primitive if Types.isSupported(primitive) =>
          primitive
        case unsupported =>
          val message = s"Data type $unsupported of the field $fieldPrefix isn't supported by spark-datafaker-connector yet."
          if (config.silentCheck) {
            logger.warn(message)
            StringType
          } else {
            throw new IllegalArgumentException(message)
          }
      }
    }

    StructType(
      schema.fields.map { field =>
        val validatedType = validateDataType(field.dataType, config, DataGenerator.fieldName(field, fieldsPrefix))
        field.copy(dataType = validatedType)
      }
    )
  }


  private def parseConfig[T: Decoder](properties: util.Map[String, String]): T = {
    val jsonMap: Map[String, Json] = properties.asScala.toMap.map {
      case (key, value) =>
        parse(value).getOrElse(Json.fromString(value)) match {
          case parsedJson => key -> parsedJson
        }
    }

    JsonObject
      .fromMap(jsonMap)
      .asJson
      .as[T]
      .fold(
        error => throw new IllegalArgumentException(s"Failed to decode Config: ${error.getMessage}"),
        identity
      )
  }

  private def parseFields(properties: util.Map[String, String]): Map[String, FieldConfig] = {
    properties.asScala
      .filter { case (key, _) => key.startsWith("fields.") }
      .map { case (key, value) =>
        val fieldPath = key.stripPrefix("fields.")
        try {
          fieldPath -> parseField(value)
        } catch {
          case ex: IllegalArgumentException =>
            throw new IllegalArgumentException(s"""Couldn't parse field "$fieldPath": ${ex.getMessage}""")
        }
      }
      .toMap
  }

  private def parseField(fieldPropertyValue: String): FieldConfig = {

    val patternJson = """(\{.*\})""".r

    fieldPropertyValue.strip() match {
      case patternJson(value) =>
        implicit val fieldConfigDecoder: Decoder[FieldConfig] = deriveDecoder[FieldConfig]
        decode[FieldConfig](value)
          .fold(
            error => throw new IllegalArgumentException(s"${error.getMessage} - $value"),
            identity
          )
      case value => FieldConfig(expression = value)
    }
  }

  override def supportsExternalMetadata(): Boolean = true

}
