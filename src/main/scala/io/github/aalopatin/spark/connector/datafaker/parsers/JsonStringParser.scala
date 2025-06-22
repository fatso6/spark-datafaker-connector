package io.github.aalopatin.spark.connector.datafaker.parsers

import io.circe.parser.{decode, parse}
import io.circe.{Decoder, KeyDecoder}

/**
 * Utility object for parsing JSON strings into Scala collections.
 *
 * <p>Provides helper methods to convert JSON strings into:
 * <ul>
 *   <li>{@code Seq[String]} if the input is a JSON array of strings</li>
 *   <li>{@code Map[String, String]} if the input is a JSON object with string keys and values</li>
 * </ul>
 *
 * <p>Throws an {@link IllegalArgumentException} if the input is not valid JSON or doesn't match the expected structure.
 */
object JsonStringParser {

  /**
   * Parses a JSON string representing an array of strings into a Scala sequence.
   *
   * <p>Example input: {@code ["en", "fr", "de"]}
   *
   * @param string the JSON string to parse
   * @return a sequence of strings
   * @throws IllegalArgumentException if the input is not a valid JSON array of strings
   */
  def parseArray(string: String): Seq[String] = {
    parse(string) match {
      case Right(json) =>
        json.asArray
          .getOrElse(throw new IllegalArgumentException("Expected JSON array"))
          .flatMap(_.asString)
      case Left(failure) =>
        throw new IllegalArgumentException(s"Invalid JSON: ${failure.getMessage}")
    }
  }

  /**
   * Parses a JSON string representing a string-to-string map into a Scala {@code Map}.
   *
   * <p>Example input: {@code {"lang": "en", "region": "US"}}
   *
   * @param string the JSON string to parse
   * @return a map of string keys and values
   * @throws IllegalArgumentException if the input is not a valid JSON object or values are not strings
   */
  def parseMap(string: String): Map[String, String] = {
    implicit val mapDecoder: Decoder[Map[String, String]] = Decoder.decodeMap(KeyDecoder.decodeKeyString, Decoder.decodeString)
    decode[Map[String, String]](string) match {
      case Right(value) => value
      case Left(_) => throw new IllegalArgumentException(s"Failed to parse Map from JSON string: $string")

    }
  }

}
