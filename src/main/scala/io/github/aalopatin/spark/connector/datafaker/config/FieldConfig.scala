package io.github.aalopatin.spark.connector.datafaker.config

import io.circe.Printer
import io.circe.syntax._
import io.circe.generic.auto._

import scala.language.implicitConversions

/**
 * Configuration for generating fields using Datafaker expressions.
 *
 * @param expression Optional Datafaker expression to generate a simple field value.
 * @param key Optional Datafaker expression for the key in a `MapType` column.
 *            Must be set together with `value` to form valid key-value pairs.
 *            If only one is set without the other, an exception is thrown.
 * @param value Optional Datafaker expression for the value in a `MapType` column.
 *              Must be set together with `key`.
 * @param count Optional number specifying how many elements to generate
 *              for an `ArrayType` or repeated entries in a `MapType` column.
 *
 * @throws IllegalArgumentException if only one of `key` or `value` is set while the other is empty.
 */
case class FieldConfig(
                        expression: Option[String],
                        key: Option[String],
                        value: Option[String],
                        count: Option[Int]
                      ) {

  require(
    key.isEmpty == value.isEmpty,
    "Either both key and value must be set, or neither"
  )

  override def toString: String = {
    val noNullsPrinter = Printer.noSpaces.copy(dropNullValues = true)
    this.asJson.printWith(noNullsPrinter)
  }
}

object FieldConfig {
  /**
   * Creates a new [[FieldConfig]] instance from optional parameters.
   *
   * @param expression Optional Datafaker expression to generate a simple field value.
   * @param key Optional Datafaker expression for the key in a `MapType` column.
   *            Must be set together with `value` to form valid key-value pairs.
   * @param value Optional Datafaker expression for the value in a `MapType` column.
   *              Must be set together with `key`.
   * @param count Optional number specifying how many elements to generate
   *              for an `ArrayType` or repeated entries in a `MapType` column.
   * @return A new `FieldConfig` instance with all fields wrapped in `Option`.
   */
  def apply(
             expression: String = null,
             key: String = null,
             value: String = null,
             count: Integer = null
           ): FieldConfig = {
    FieldConfig(
      Option(expression),
      Option(key),
      Option(value),
      Option(count).map(_.toInt)
    )
  }

  object implicits {
    /**
     * Implicit conversion from [[FieldConfig]] to its JSON string representation.
     */
    implicit def fieldConfigToString(fieldConfig: FieldConfig): String = fieldConfig.toString
  }

}


