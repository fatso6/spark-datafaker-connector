package io.github.aalopatin.spark.connector.datafaker.generation

import org.apache.spark.sql.catalyst.util.GenericArrayData
import org.apache.spark.sql.types.DataType

/**
 * Utility object for generating Spark SQL-compatible array data from raw string inputs.
 */
object UtilGenerator {
  /**
   * Converts a sequence of strings into a {@link org.apache.spark.sql.catalyst.util.GenericArrayData} instance,
   * with each string transformed into the appropriate type according to the specified {@code DataType}.
   *
   * <p>An optional {@code valueProvider} function can be supplied to transform each input string
   * before conversion (e.g. to apply a faker expression or a predefined transformation).
   *
   * @param inputs        Sequence of string values to convert
   * @param dataType      Target Spark SQL {@code DataType} for each element in the array
   * @param valueProvider Optional transformation function applied to each input string before type conversion;
   *                      defaults to identity (no transformation)
   * @return              {@code GenericArrayData} containing converted values
   * @throws IllegalArgumentException if a value cannot be converted to the specified data type
   */
  def generateArrayDataFromStrings(inputs: Seq[String], dataType: DataType, valueProvider: String => String = identity) = {
    new GenericArrayData(
      inputs.map(
        input =>
          Types.convertValue(
            valueProvider(input),
            dataType
          )
      )
    )
  }
}
