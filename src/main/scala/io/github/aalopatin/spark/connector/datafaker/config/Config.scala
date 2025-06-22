package io.github.aalopatin.spark.connector.datafaker.config

/**
 * Configuration options for the Spark Datafaker Connector.
 *
 * @param silentCheck whether to run data types checks silently (default: true)
 * @param locale language locale code, e.g., "en" (default: "en")
 * @param numRows number of rows to generate; must be positive (default: 10)
 * @throws IllegalArgumentException if numRows is not positive
 */
case class Config(
                   silentCheck: Boolean = true,
                   locale: String = "en",
                   numRows: Int = 10
                 ) {
  require(numRows > 0, "numRows must be positive")
}