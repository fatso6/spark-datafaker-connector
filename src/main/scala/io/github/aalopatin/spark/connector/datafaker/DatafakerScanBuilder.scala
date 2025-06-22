package io.github.aalopatin.spark.connector.datafaker

import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig, PartitionConfig}
import org.apache.spark.sql.connector.read.{Scan, ScanBuilder}
import org.apache.spark.sql.types.StructType

class DatafakerScanBuilder(
                            val schema: StructType,
                            val config: Config,
                            val partitionConfig: PartitionConfig,
                            val fields: Map[String, FieldConfig]
                          ) extends ScanBuilder {
  override def build(): Scan = new DatafakerScan(schema, config, partitionConfig, fields)
}
