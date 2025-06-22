package io.github.aalopatin.spark.connector.datafaker

import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig, PartitionConfig}
import io.github.aalopatin.spark.connector.datafaker.generation.{DataGenerator, Partition, PartitionGenerator}
import org.apache.spark.sql.connector.read.{Batch, InputPartition, PartitionReaderFactory, Scan}
import org.apache.spark.sql.types.StructType
import org.slf4j.LoggerFactory

class DatafakerScan(val schema: StructType, val config: Config, val partitionConfig: PartitionConfig, val fields: Map[String, FieldConfig]) extends Scan with Batch {

  override def readSchema(): StructType = schema

  override def toBatch: Batch = this

  override def planInputPartitions(): Array[InputPartition] = {
    val generator = new PartitionGenerator(config, schema, fields)
    partitionConfig match {
      case PartitionConfig(Some(partitionsColumn), Some(partitionsValues), None) =>
        val values = generator.parsePartitionsValues(partitionsColumn, partitionsValues)
        generateInputPartitions(partitionsColumn, values)
      case PartitionConfig(Some(partitionsColumn), None, Some(partitionsCount)) =>
        val values = generator.generatePartitionsValues(partitionsColumn, partitionsCount)
        generateInputPartitions(partitionsColumn, values)
      case PartitionConfig(None, None, None) => Array(new DatafakerInputPartition(schema, config, fields))
    }

  }

  private def generateInputPartitions(partitionsColumn: String, partitionsValues: List[Any]): Array[InputPartition] = {
    partitionsValues.toArray.map { value =>
      new DatafakerInputPartition(schema, config, fields, Some(Partition(partitionsColumn, value)))
    }
  }

  override def createReaderFactory(): PartitionReaderFactory = new DatafakerPartitionReaderFactory()
}
