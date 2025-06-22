package io.github.aalopatin.spark.connector.datafaker.generation

import DataGenerator.fieldName
import UtilGenerator.generateArrayDataFromStrings
import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig}
import io.github.aalopatin.spark.connector.datafaker.parsers.JsonStringParser.{parseArray}
import net.datafaker.Faker
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.catalyst.util.{ArrayBasedMapData, GenericArrayData, MapData}
import org.apache.spark.sql.types._

import java.util.Locale

/**
 * Generates data rows for a given schema and field configuration using the Datafaker library.
 *
 * <p>This class handles primitive, struct, array, and map types defined in the Spark schema and
 * generates corresponding fake data using field-specific configuration rules. It can also apply
 * constant partition values if required.
 *
 * @param config The global configuration (locale, number of rows, etc.)
 * @param schema The Spark schema that defines the structure of the generated data
 * @param fields A map of field names to {@link FieldConfig} that defines generation logic
 */
class DataGenerator (config: Config, schema: StructType, fields: Map[String, FieldConfig]) {
  private val faker = new Faker(new Locale(config.locale))

  /**
   * Generates a single row of data based on the provided schema and optional partition value.
   *
   * @param partition Optional partition data to override field values
   * @param prefix Prefix used to resolve nested fields (e.g., for StructTypes)
   * @return A {@link GenericInternalRow} populated with fake data
   */
  def generateRow(partition: Option[Partition])(implicit prefix: String = ""): GenericInternalRow = {
    val values = schema.fields.map { field =>
      generateValue(field, partition)
    }
    new GenericInternalRow(values)
  }

  /**
   * Generates a single value for a schema field, respecting data type and partition overrides.
   *
   * @param field The field to generate a value for
   * @param partition Optional partition that may override the value
   * @param prefix Prefix for nested field names
   * @return Generated value in a format compatible with Spark SQL types
   */
  private[generation] def generateValue(field: StructField, partition: Option[Partition])(implicit prefix: String): Any = {
    partition match {
      case Some(Partition(field.name, partitionValue)) => partitionValue
      case _ => field.dataType match {
        case _: StructType => generateStructType(field)
        case _: ArrayType => generateArrayType(field)
        case _: MapType => generateMapType(field)
        case _ => generatePrimitiveType(field)
      }
    }
  }

  /** Generates a value for a StructType field. */
  private def generateStructType(field: StructField)(implicit prefix: String) = {
    val name = fieldName(field, prefix)
    val structType = field.dataType.asInstanceOf[StructType]
    val generator = new DataGenerator(config, structType, fields)
    generator.generateRow(None)(name)
  }

  /** Generates an ArrayData object for an ArrayType field. */
  private def generateArrayType(field: StructField)(implicit prefix: String) = {
    val name = fieldName(field, prefix)
    val fieldConfig = getFieldConfig(field.name, prefix)
    val ArrayType(elementType, containsNull) = field.dataType
    generateArrayData(StructField("value", elementType, containsNull), fieldConfig.expression, fieldConfig.count)(name)
  }

  /** Generates a MapData object for a MapType field. */
  private def generateMapType(field: StructField)(implicit prefix: String) = {

    val name = fieldName(field, prefix)

    val fieldConfig = getFieldConfig(field.name, prefix)
    val MapType(keyDataType, valueDataType, valueContainsNull) = field.dataType
    new ArrayBasedMapData(
      generateArrayData(StructField("key", keyDataType), fieldConfig.key, fieldConfig.count)(name),
      generateArrayData(StructField("value", valueDataType, valueContainsNull), fieldConfig.value, fieldConfig.count)(name)
    )

  }

  /**
   * Dispatches array generation depending on element type (primitive or complex).
   *
   * @param structField Field metadata for the array element
   * @param expression Expression(s) to use for primitive types
   * @param count Number of elements to generate
   */
  private def generateArrayData(structField: StructField, expression: Option[String], count: Option[Int])(implicit prefix: String): GenericArrayData = {

    if (Types.isComplex(structField.dataType)) {
      generateArrayDataComplex(structField, count)
    } else {
      generateArrayDataPrimitive(structField, expression, count)
    }

  }

  /**
   * Generates array data for a complex element type (e.g., struct).
   *
   * @param structField Field metadata for the complex element
   * @param count Required number of elements
   */
  private def generateArrayDataComplex(structField: StructField, count: Option[Int])(implicit prefix: String) = {
    val values = count match {
      case Some(count) => (1 to count).map(_ => generateValue(structField, None))
      case None => throw new IllegalArgumentException(s"Missing 'count' for complex array field: ${prefix}")
    }
    new GenericArrayData(values)
  }

  /**
   * Generates array data for a primitive element type (e.g., string, int).
   *
   * @param structField Field metadata
   * @param expression Faker expression or JSON array string
   * @param count Number of elements to generate
   */
  private def generateArrayDataPrimitive(structField: StructField, expression: Option[String], count: Option[Int])(implicit prefix: String) = {
    val name = fieldName(structField.name, prefix)
    val expressions = count match {
      case Some(count) => (1 to count).map(_ => expression.getOrElse(throw new IllegalArgumentException(s"Missing 'expression' for primitive array elements of field: $name")))
      case None =>
        val expressionValue = expression.getOrElse(throw new IllegalArgumentException(s"Missing 'expression' (JSON array string) for array elements of field: $name"))
        try {
          parseArray(expressionValue)
        } catch {
          case e: IllegalArgumentException => throw new IllegalArgumentException(s"Error parsing array expressions for field '$name': ${e.getMessage}", e)
        }
    }
    generateArrayDataFromStrings(expressions, structField.dataType, faker.expression)
  }

  /** Generates a primitive value and converts it to the correct data type. */
  private def generatePrimitiveType(field: StructField)(implicit prefix: String) = {
    val value = generateStringValue(field)
    Types.convertValue(value, field.dataType)
  }

  /** Returns a generated string from the faker expression defined in config. */
  private def generateStringValue(field: StructField)(implicit prefix:String): String = {
    val fieldConfig = getFieldConfig(field.name, prefix)
    val expression = fieldConfig.expression.getOrElse(
      throw new IllegalArgumentException(s"Missing 'expression' for primitive field: ${fieldName(field.name, prefix)}")
    )
    faker.expression(expression)
  }

  /** Looks up the {@link FieldConfig} for a given field. */
  private def getFieldConfig(name: String, prefix: String) = {
    val fieldConfigName = fieldName(name, prefix)
    fields.getOrElse(fieldConfigName, throw new IllegalArgumentException(s"Missing config for field ${fieldConfigName}"))
  }

}

/**
 * Companion object for {@link DataGenerator} that provides utility methods for nested field naming.
 */
object DataGenerator {
  /**
   * Builds the full field name using a prefix and {@link StructField}.
   *
   * @param field StructField metadata
   * @param prefix Optional nested field prefix
   * @return Resolved full field name
   */
  def fieldName(field: StructField, prefix:String): String = {
    fieldName(field.name, prefix)
  }

  /**
   * Builds the full field name using a prefix and a string field name.
   *
   * @param fieldName The base field name
   * @param prefix The prefix to prepend
   * @return Combined full field name
   * @throws IllegalArgumentException if both prefix and field name are empty
   */
  def fieldName(fieldName: String, prefix: String): String = {
    val safeFieldName = Option(fieldName).getOrElse("")
    val safePrefix = Option(prefix).getOrElse("")

    (safePrefix.isEmpty, safeFieldName.isEmpty) match {
      case (true, false)  => safeFieldName
      case (false, true)  => safePrefix
      case (false, false) => s"$safePrefix.$safeFieldName"
      case (true, true)   => throw new IllegalArgumentException("Both prefix and fieldName cannot be empty")
    }
  }
}
