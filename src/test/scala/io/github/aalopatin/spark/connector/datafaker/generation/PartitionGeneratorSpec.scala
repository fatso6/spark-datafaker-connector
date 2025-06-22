package io.github.aalopatin.spark.connector.datafaker.generation

import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig}
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.catalyst.util.{ArrayBasedMapData, GenericArrayData}
import org.apache.spark.sql.types.{ArrayType, IntegerType, MapType, StringType, StructField, StructType}
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PartitionGeneratorSpec extends AnyFlatSpec with Matchers with PrivateMethodTester {

  private val DummyConfig = Config()
  private val DummyFields = Map.empty[String, FieldConfig]

  behavior of "parsePartitionsValues"

  it should "parse primitive Int partition values" in {
    val schema = StructType(Seq(StructField("id", IntegerType)))
    val generator = new PartitionGenerator(DummyConfig, schema, DummyFields)

    val result = generator.parsePartitionsValues("id", List("1", "2", "3"))

    result shouldEqual List(1, 2, 3)
  }

  it should "parse array of strings partition values" in {
    val schema = StructType(Seq(StructField("tags", ArrayType(StringType))))
    val generator = new PartitionGenerator(DummyConfig, schema, DummyFields)

    val partitionsValue = List(
      "[\"1\",\"2\",\"3\"]",
      "[\"4\",\"5\",\"6\"]",
    )
    val result = generator.parsePartitionsValues("tags", partitionsValue)
    val expected = List(
      new GenericArrayData(Seq(
        UTF8String.fromString("1"),
        UTF8String.fromString("2"),
        UTF8String.fromString("3")
      )),
      new GenericArrayData(Seq(
        UTF8String.fromString("4"),
        UTF8String.fromString("5"),
        UTF8String.fromString("6")
      ))
    )

    result should have size 2
    result shouldBe expected
  }

  it should "parse map of string to int partition values" in {
    val mapType = MapType(StringType, IntegerType)

    val schema = StructType(Seq(
      StructField("scores", mapType)
    ))

    val generator = new PartitionGenerator(DummyConfig, schema, DummyFields)

    val partitionsValue = List(
      """{"math":"90","english":"85"}""",
      """{"history":"75","science":"95"}"""
    )

    val result = generator.parsePartitionsValues("scores", partitionsValue)

    val expectedHead =
      new ArrayBasedMapData(
        new GenericArrayData(Seq(
          UTF8String.fromString("math"),
          UTF8String.fromString("english")
        )),
        new GenericArrayData(Seq(90, 85))
      )

    result should have size 2
    val head = result.head.asInstanceOf[ArrayBasedMapData]
    head.keyArray shouldBe expectedHead.keyArray
    head.valueArray shouldBe expectedHead.valueArray
  }

  it should "parse struct partition values" in {
    val structType = StructType(Seq(
      StructField("name", StringType),
      StructField("age", IntegerType)
    ))

    val schema = StructType(Seq(
      StructField("person", structType)
    ))

    val generator = new PartitionGenerator(DummyConfig, schema, DummyFields)

    val partitionsValue = List(
      """{"name":"Alice","age":"30"}""",
      """{"name":"Bob","age":"25"}"""
    )

    val result = generator.parsePartitionsValues("person", partitionsValue)

    val expected = List(
      new GenericInternalRow(Array(
        UTF8String.fromString("Alice"),
        30
      )),
      new GenericInternalRow(Array(
        UTF8String.fromString("Bob"),
        25
      ))
    )

    result should have size 2
    result shouldBe expected
  }

  it should "throws when struct JSON is missing field" in {
    val structType = StructType(Seq(
      StructField("name", StringType),
      StructField("age", IntegerType)
    ))
    val schema = StructType(Seq(StructField("meta", structType)))
    val generator = new PartitionGenerator(DummyConfig, schema, DummyFields)

    val exception = intercept[IllegalArgumentException] {
      generator.parsePartitionsValues("meta", List("""{"name": "Alex"}"""))
    }

    exception.getMessage should include ("Missing key 'age'")
  }

  behavior of "generatePartitionsValues"

  it should "generate partition values" in {
    val schema = StructType(Seq(StructField("id", IntegerType)))
    val fieldConfigs = Map("id" -> FieldConfig("#{number.numberBetween '1','100'}"))
    val generator = new PartitionGenerator(DummyConfig, schema, fieldConfigs)

    val result = generator.generatePartitionsValues("id", 5)

    result should have size 5
    all(result) shouldBe a [Integer]
  }

}
