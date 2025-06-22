package io.github.aalopatin.spark.connector.datafaker.generation

import UtilGenerator.generateArrayDataFromStrings
import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig}
import io.github.aalopatin.spark.connector.datafaker.parsers.JsonStringParser.{parseArray, parseMap}
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.catalyst.util.ArrayBasedMapData
import org.apache.spark.sql.types.{ArrayType, DataType, MapType, StructType}

/**
 * Generates and parses partition values used to produce partitioned datasets.
 *
 * <p>This class supports both predefined (manual) and automatically generated partitions for
 * primitive and complex types including {@code StructType}, {@code ArrayType}, and {@code MapType}.
 *
 * <p>Partition values are used to generate datasets where a specific column (or nested field)
 * is held constant across rows. This can be helpful when simulating partitioned datasets
 * in Spark for testing or mock data generation.
 *
 * @param config  The configuration for data generation, including locale and number of rows
 * @param schema  The full schema of the dataset being generated
 * @param fields  A map of field configurations, used by the data generator for value creation
 */
class PartitionGenerator(config: Config, schema: StructType, fields: Map[String, FieldConfig]) {
  /**
   * Parses a list of raw string partition values into typed values for the given column.
   *
   * <p>The input values must be formatted according to the column's type. For complex types,
   * values must be JSON strings. For example:
   * <ul>
   *   <li>{@code StructType}: {"key1": "value1", "key2": 2}</li>
   *   <li>{@code ArrayType}: ["one", "two"]</li>
   *   <li>{@code MapType}: {"en": "1", "fr": "2"}</li>
   * </ul>
   *
   * @param partitionsColumn  The name of the column to apply the values to
   * @param partitionsValues  List of raw string values (usually JSON for complex types)
   * @return                  A list of typed values corresponding to the column's data type
   */
  def parsePartitionsValues(partitionsColumn: String, partitionsValues: List[String]): List[Any] = {
    val field = schema(partitionsColumn)
    field.dataType match {
      case structType: StructType => parseStructType(structType, partitionsValues)
      case arrayType: ArrayType => parseArrayType(arrayType, partitionsValues)
      case mapType: MapType => parseMapType(mapType, partitionsValues)
      case dataType => parsePrimitiveType(dataType, partitionsValues)
    }
  }

  private def parseStructType(structType: StructType, partitionsValues: List[String]) = {
    partitionsValues.map { partition =>
      val partitionMap = parseMap(partition)
      val fields = structType.fields.map { field =>
        val fieldName = field.name
        val fieldValue = partitionMap.getOrElse(
          fieldName,
          throw new IllegalArgumentException(s"Missing key '$fieldName' in struct partition JSON: $partition")
        )
        Types.convertValue(fieldValue, field.dataType)
      }
      new GenericInternalRow(fields)
    }
  }

  private def parseArrayType(arrayType: ArrayType, partitionsValues: List[String]) = {
    partitionsValues.map { partition =>
      val partitionArray = parseArray(partition)
      generateArrayDataFromStrings(partitionArray, arrayType.elementType)
    }
  }

  private def parseMapType(mapType: MapType, partitionsValues: List[String]) = {
    partitionsValues.map { partition =>
      val partitionMap = parseMap(partition)
      new ArrayBasedMapData(
        generateArrayDataFromStrings(partitionMap.keys.toSeq, mapType.keyType),
        generateArrayDataFromStrings(partitionMap.values.toSeq, mapType.valueType)
      )

    }
  }

  private def parsePrimitiveType(dataType: DataType, partitionsValues: List[String]) = {
    partitionsValues.map { partition =>
      Types.convertValue(partition, dataType)
    }
  }

  /**
   * Automatically generates a fixed number of partition values for a column.
   *
   * <p>Uses {@code DataGenerator} to produce realistic values based on the field config.
   * This method can be used when {@code partitionsValues} is not explicitly provided.
   *
   * @param partitionsColumn  The name of the column to generate values for
   * @param partitionsCount   The number of partition values to generate
   * @return                  A list of generated values for the specified column
   */
  def generatePartitionsValues(partitionsColumn: String, partitionsCount: Int): List[Any] = {
    val dataGenerator = new DataGenerator(config, schema, fields)
    val field = schema(partitionsColumn)
    (1 to partitionsCount).map(
      _ => dataGenerator.generateValue(field, None)("")
    ).toList
  }

}
