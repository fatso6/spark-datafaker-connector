package io.github.aalopatin.spark.connector.datafaker

import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig, PartitionConfig}
import org.apache.spark.sql.connector.catalog.{SupportsRead, TableCapability}
import org.apache.spark.sql.connector.read.ScanBuilder
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.util.CaseInsensitiveStringMap

import java.util

class DatafakerTable(val schema: StructType, val config: Config, val partitionConfig: PartitionConfig, val fields: Map[String, FieldConfig]) extends SupportsRead {
  override def name(): String = "datafaker_table"
  override def capabilities(): util.Set[TableCapability] = util.EnumSet.of(TableCapability.BATCH_READ)

  override def newScanBuilder(options: CaseInsensitiveStringMap): ScanBuilder = new DatafakerScanBuilder(schema, config, partitionConfig, fields)
}
