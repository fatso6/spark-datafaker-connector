package io.github.aalopatin.spark.connector.datafaker.parsers

import java.sql.Timestamp
import java.time.{Instant, LocalDate}

/**
 * Utility object for parsing date/time strings into appropriate Java time types.
 *
 * <p>This parser supports three date/time formats and converts them into corresponding types:
 * <ul>
 * <li>{@code yyyy-MM-dd} → {@link LocalDate}</li>
 * <li>{@code yyyy-MM-ddTHH:mm:ss[.SSS]Z} → {@link Instant}</li>
 * <li>{@code yyyy-MM-dd HH:mm:ss[.SSS]} → {@link java.sql.Timestamp}</li>
 * </ul>
 *
 * <p>If the input string doesn't match any of the supported formats, an {@link IllegalArgumentException} is thrown.
 */
object DateTimeParser {
  // Matches strings like "2024-06-18"
  private val LocalDatePattern = """\d{4}-\d{2}-\d{2}""".r

  // Matches strings like "2024-06-18T15:30:00Z" or "2024-06-18T15:30:00.123Z"
  private val InstantPattern = """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?Z""".r

  // Matches strings like "2024-06-18 15:30:00" or "2024-06-18 15:30:00.123"
  private val TimestampPattern = """\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}(?:\.\d+)?""".r

  /**
   * Parses a date/time string into an appropriate Java time object.
   *
   * <p>This method returns:
   * <ul>
   * <li>{@link LocalDate} if the string is in {@code yyyy-MM-dd} format</li>
   * <li>{@link Instant} if the string is in ISO-8601 UTC format (ending with {@code Z})</li>
   * <li>{@link java.sql.Timestamp} if the string contains both date and time separated by a space</li>
   * </ul>
   *
   * @param value the input string to parse
   * @return a {@link LocalDate}, {@link Instant}, or {@link java.sql.Timestamp} object
   * @throws IllegalArgumentException if the string does not match any supported formats
   */
  def parse(value: String) = value match {
    case LocalDatePattern() => LocalDate.parse(value)
    case InstantPattern() => Instant.parse(value)
    case TimestampPattern() => Timestamp.valueOf(value)
    case _ => throw new IllegalArgumentException(s"Unrecognized date/time format: '$value'. Expected formats: yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss[.SSS]Z, yyyy-MM-dd HH:mm:ss[.SSS]")
  }
}
