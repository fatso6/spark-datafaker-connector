package io.github.aalopatin.spark.connector.datafaker

import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig}
import io.github.aalopatin.spark.connector.datafaker.generation.Partition
import org.apache.spark.sql.connector.read.InputPartition
import org.apache.spark.sql.types.StructType

class DatafakerInputPartition(
                               val schema: StructType,
                               val config: Config,
                               val fields: Map[String, FieldConfig],
                               val partition: Option[Partition] = None
                             ) extends InputPartition