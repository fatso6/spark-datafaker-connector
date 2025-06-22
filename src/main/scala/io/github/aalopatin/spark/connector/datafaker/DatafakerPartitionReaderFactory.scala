package io.github.aalopatin.spark.connector.datafaker

import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.{InputPartition, PartitionReader, PartitionReaderFactory}

class DatafakerPartitionReaderFactory() extends PartitionReaderFactory {
  override def createReader(partition: InputPartition): PartitionReader[InternalRow] =
    new DatafakerPartitionReader(partition.asInstanceOf[DatafakerInputPartition])
}