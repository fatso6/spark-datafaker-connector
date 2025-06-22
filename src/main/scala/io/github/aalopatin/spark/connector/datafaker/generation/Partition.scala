package io.github.aalopatin.spark.connector.datafaker.generation

/**
 * Represents a partition constraint to be applied during data generation.
 *
 * <p>This class is used to define a fixed value for a specific column, typically to generate
 * partitioned datasets where one or more columns have constant values across rows.
 *
 * @param partitionsColumn The name of the column to apply the partition value to
 * @param partitionValue   The constant value to assign to the specified column
 */
case class Partition(partitionsColumn: String, partitionValue: Any)
