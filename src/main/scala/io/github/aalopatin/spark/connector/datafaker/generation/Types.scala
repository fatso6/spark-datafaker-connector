package io.github.aalopatin.spark.connector.datafaker.generation

import io.github.aalopatin.spark.connector.datafaker.parsers.DateTimeParser
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String

import java.sql.Timestamp
import java.time.{Instant, LocalDate, LocalDateTime, ZoneOffset}

/**
 * Utility object for working with Spark SQL {@link DataType}s, including type checking and value conversion.
 *
 * <p>This class provides methods to:
 * <ul>
 *   <li>Check if a type is supported for conversion</li>
 *   <li>Convert string values to Spark internal representations</li>
 *   <li>Determine whether a type is complex (e.g. StructType, MapType, or ArrayType)</li>
 * </ul>
 *
 * <p>It supports common primitive types and basic date/time types including:
 * String, Byte, Short, Integer, Long, Float, Double, Boolean, Date, and Timestamp.
 *
 * <p>Complex types (StructType, MapType, ArrayType) are handled elsewhere using recursive logic.
 */
object Types {

  private val supportedTypes: Set[DataType] = Set(
    StringType, ByteType, ShortType, IntegerType,
    LongType, FloatType, DoubleType, BooleanType,
    DateType, TimestampType
  )

  /**
   * Converts a raw string value to a Spark-compatible internal representation based on the given {@code DataType}.
   *
   * @param value     The string value to convert
   * @param dataType  The target Spark SQL data type
   * @return          Converted value in Spark-compatible format (e.g., UTF8String, Int, Long)
   * @throws IllegalArgumentException if the type is unsupported or value is invalid
   */
  def convertValue(value: String, dataType: DataType): Any = {
    dataType match {
      case StringType => UTF8String.fromString(value)
      case ByteType => value.toByte
      case ShortType => value.toShort
      case IntegerType => value.toInt
      case LongType => value.toLong
      case FloatType => value.toFloat
      case DoubleType => value.toDouble
      case BooleanType => value.toBoolean
      case DateType => convertDateValue(value)
      case TimestampType => convertTimestampValue(value)
      case _ => throw new IllegalArgumentException(s"Unsupported data type: $dataType")
    }
  }

  private def convertDateValue(value: String) = {
    val date = DateTimeParser.parse(value)
    date match {
      case d: LocalDate => d.toEpochDay.toInt
      case i: Instant => i.atZone(ZoneOffset.UTC).toLocalDate.toEpochDay.toInt
      case t: Timestamp => t.toLocalDateTime.toLocalDate.toEpochDay.toInt
    }
  }

  private def convertTimestampValue(value: String) = {

    val date = DateTimeParser.parse(value)
    date match {
      case d: LocalDate => d.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli * 1000
      case i: Instant => i.toEpochMilli * 1000
      case t: Timestamp => t.toInstant.toEpochMilli * 1000
    }

  }

  /**
   * Checks whether the given {@code DataType} is supported by the conversion logic.
   *
   * @param dataType The type to check
   * @return         {@code true} if supported; {@code false} otherwise
   */
  def isSupported(dataType: DataType): Boolean = {
      supportedTypes.contains(dataType)
  }

  /**
   * Determines whether a given {@code DataType} is complex (i.e., StructType, ArrayType, or MapType).
   *
   * @param dataType The type to inspect
   * @return         {@code true} if it's a complex type; {@code false} if it's primitive
   */
  def isComplex(dataType: DataType): Boolean = {
    dataType match {
      case _: StructType | _: MapType | _: ArrayType => true
      case _ => false
    }
  }

}
