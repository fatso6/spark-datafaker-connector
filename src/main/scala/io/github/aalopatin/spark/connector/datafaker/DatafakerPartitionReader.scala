package io.github.aalopatin.spark.connector.datafaker

import io.github.aalopatin.spark.connector.datafaker.generation.DataGenerator
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.connector.read.PartitionReader

class DatafakerPartitionReader(inputPartition: DatafakerInputPartition) extends PartitionReader[InternalRow] {

  private val generator = new DataGenerator(
    inputPartition.config,
    inputPartition.schema,
    inputPartition.fields
  )

  private var index = 0

  override def next(): Boolean = {
    if (index < inputPartition.config.numRows) {
      index += 1
      true
    } else {
      false
    }
  }

  override def get(): InternalRow = generator.generateRow(inputPartition.partition)

  override def close(): Unit = {}

}