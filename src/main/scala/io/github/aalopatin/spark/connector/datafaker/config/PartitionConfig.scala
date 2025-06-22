package io.github.aalopatin.spark.connector.datafaker.config

/**
 * Configuration class for defining how the generated dataset should be partitioned.
 *
 * This is typically used when generating synthetic data with specific partitioning requirements
 * for performance, organization, or parallelism.
 *
 * @param partitionsColumn Optional name of the column used for partitioning the dataset.
 *                         If set, the dataset will be partitioned by this column.
 * @param partitionsValues Optional list of values to be used as partitions for the specified column.
 *                         When provided, these values will be assigned across partitions in order.
 * @param partitionsCount Optional number of partitions to create.
 *                        If both `partitionsValues` and `partitionsCount` are specified,
 *                        `partitionsValues` takes precedence.
 */
case class PartitionConfig(
                            partitionsColumn: Option[String],
                            partitionsValues: Option[List[String]],
                            partitionsCount: Option[Int]
                          )
