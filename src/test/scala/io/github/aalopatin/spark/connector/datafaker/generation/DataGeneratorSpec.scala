package io.github.aalopatin.spark.connector.datafaker.generation

import io.github.aalopatin.spark.connector.datafaker.config.{Config, FieldConfig}
import org.apache.spark.sql.catalyst.expressions.GenericInternalRow
import org.apache.spark.sql.catalyst.util.{ArrayBasedMapData, GenericArrayData}
import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DataGeneratorSpec extends AnyFlatSpec with Matchers with PrivateMethodTester {

  private val DummyConfig = Config()
  private val DummyFieldConfig = FieldConfig(expression = "")
  private val DummySchema = StructType(Nil)
  private val DummyFields = Map.empty[String, FieldConfig]
  private val DummyPrefix = ""

  behavior of "fieldName(fieldName, prefix)"

  it should "return fieldName when prefix is empty" in {
    DataGenerator.fieldName("field", "") shouldBe "field"
  }

  it should "return prefix when fieldName is empty" in {
    DataGenerator.fieldName("", "prefix") shouldBe "prefix"
  }

  it should "concatenate prefix and fieldName with a dot when both are non-empty" in {
    DataGenerator.fieldName("field", "prefix") shouldBe "prefix.field"
  }

  it should "throw IllegalArgumentException when both are empty" in {
    val thrown = intercept[IllegalArgumentException] {
      DataGenerator.fieldName("", "")
    }
    thrown.getMessage should include ("Both prefix and fieldName cannot be empty")
  }

  behavior of "fieldName(field, prefix)"

  it should "delegate to fieldName(field.name, prefix)" in {
    val field = StructField("fieldNameHere", StringType)
    DataGenerator.fieldName(field, "prefix") shouldBe "prefix.fieldNameHere"
  }

  behavior of "getFieldConfig"

  it should "return the correct FieldConfig when present" in {
    val fields = Map("person.name" -> DummyFieldConfig)
    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val getFieldConfig = PrivateMethod[FieldConfig](Symbol("getFieldConfig"))
    val result = generator invokePrivate getFieldConfig("name", "person")

    result shouldBe DummyFieldConfig
  }

  it should "throw IllegalArgumentException when config is missing" in {
    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val getFieldConfig = PrivateMethod[FieldConfig](Symbol("getFieldConfig"))
    val thrown = intercept[IllegalArgumentException] {
      generator invokePrivate getFieldConfig("name", "person")
    }
    thrown.getMessage should include("Missing config for field person.name")
  }

  behavior of "generateStringValue"

  it should "return the evaluated expression from faker when present" in {
    val schema = StructType(Seq(
      StructField("name", StringType)
    ))
    val fieldConfig = FieldConfig(expression = "Alice")
    val fields = Map("name" -> fieldConfig)
    val generator = new DataGenerator(DummyConfig, schema, fields)
    val generateStringValue = PrivateMethod[String](Symbol("generateStringValue"))
    val result = generator invokePrivate generateStringValue(schema("name"), DummyPrefix)
    result shouldBe "Alice"
  }

  it should "throw IllegalArgumentException when expression is missing in FieldConfig" in {
    val schema = StructType(Seq(StructField("name", StringType)))
    val fieldConfig = FieldConfig(expression = null)
    val fields = Map("name" -> fieldConfig)
    val generator = new DataGenerator(DummyConfig, schema, fields)

    val generateStringValue = PrivateMethod[String](Symbol("generateStringValue"))
    val thrown = intercept[IllegalArgumentException] {
      generator invokePrivate generateStringValue(schema("name"), DummyPrefix)
    }

    thrown.getMessage should include("Missing 'expression' for primitive field: name")
  }

  behavior of "generateArrayDataPrimitive"

  it should "generate array using repeated expression with count" in {
    val field = StructField("field", StringType)
    val expression = Some("hello")
    val count = Some(3)

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayDataPrimitive = PrivateMethod[GenericArrayData](Symbol("generateArrayDataPrimitive"))

    val result = generator invokePrivate generateArrayDataPrimitive(field, expression, count, DummyPrefix)
    val expected = new GenericArrayData(
      Seq(
        UTF8String.fromString("hello"),
        UTF8String.fromString("hello"),
        UTF8String.fromString("hello")
      )
    )
    result shouldBe expected
  }

  it should "generate array from JSON string expression when count is None" in {
    val field = StructField("array", StringType)
    val jsonArrayString = """["a", "b", "c"]"""

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayDataPrimitive = PrivateMethod[GenericArrayData](Symbol("generateArrayDataPrimitive"))

    val result = generator invokePrivate generateArrayDataPrimitive(field, Some(jsonArrayString), None, DummyPrefix)
    val expected = new GenericArrayData(
      Seq(
        UTF8String.fromString("a"),
        UTF8String.fromString("b"),
        UTF8String.fromString("c")
      )
    )

    result shouldBe expected
  }

  it should "throw IllegalArgumentException when count is Some but expression is None" in {
    val field = StructField("array", StringType)

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayDataPrimitive = PrivateMethod[GenericArrayData](Symbol("generateArrayDataPrimitive"))

    val thrown = intercept[IllegalArgumentException] {
      generator invokePrivate generateArrayDataPrimitive(field, None, Some(2), DummyPrefix)
    }
    thrown.getMessage should include("Missing 'expression' for primitive array elements")
  }

  it should "throw IllegalArgumentException when count is None and expression is not a valid JSON array" in {
    val field = StructField("array", StringType)

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayDataPrimitive = PrivateMethod[GenericArrayData](Symbol("generateArrayDataPrimitive"))

    val thrown = intercept[IllegalArgumentException] {
      generator invokePrivate generateArrayDataPrimitive(field, Some("""not a json"""), None, DummyPrefix)
    }
    thrown.getMessage should include("Error parsing array expressions")
  }

  it should "throw IllegalArgumentException when count is None and JSON is not an array" in {
    val field = StructField("array", StringType)

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayDataPrimitive = PrivateMethod[GenericArrayData](Symbol("generateArrayDataPrimitive"))

    val thrown = intercept[IllegalArgumentException] {
      generator invokePrivate generateArrayDataPrimitive(field, Some("""{"not": "an array"}"""), None, DummyPrefix)
    }
    thrown.getMessage should include("Expected JSON array")
  }

  behavior of "generateArrayDataComplex"

  it should "generate array of given count complex values with count provided" in {

    val fields = Map(
      "matrix" -> FieldConfig(count = 2),
      "matrix.value" -> FieldConfig(expression = "1", count = 2)
    )

    val field = StructField("value", ArrayType(StringType))

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val generateArrayDataComplex = PrivateMethod[GenericArrayData](Symbol("generateArrayDataComplex"))

    val result = generator invokePrivate generateArrayDataComplex(field, fields("matrix").count, "matrix")
    val expected = new GenericArrayData(Seq(
      new GenericArrayData(Seq(UTF8String.fromString("1"), UTF8String.fromString("1"))),
      new GenericArrayData(Seq(UTF8String.fromString("1"), UTF8String.fromString("1")))
    ))

    result shouldBe expected

  }

  it should "throw IllegalArgumentException if count is missing for complex array field" in {
    val field = StructField("value", ArrayType(StringType))

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayDataComplex = PrivateMethod[GenericArrayData](Symbol("generateArrayDataComplex"))

    val exception = intercept[IllegalArgumentException] {
      generator invokePrivate generateArrayDataComplex(field, None, "matrix")
    }

    exception.getMessage should include ("Missing 'count' for complex array field: matrix")
  }

  behavior of "generateArrayData"

  it should "generate array for primitive StructType" in {
    val field = StructField("myField", StringType)
    val expression = Some("hello")
    val count = Some(3)

    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)
    val generateArrayData = PrivateMethod[GenericArrayData](Symbol("generateArrayData"))

    val result = generator invokePrivate generateArrayData(field, expression, count, DummyPrefix)
    val expected = new GenericArrayData(
      Seq(
        UTF8String.fromString("hello"),
        UTF8String.fromString("hello"),
        UTF8String.fromString("hello")
      )
    )
    result shouldBe expected
  }

  it should "generate array of given count complex values with count provided" in {

    val fields = Map(
      "matrix" -> FieldConfig(count = 2),
      "matrix.value" -> FieldConfig(expression = "1", count = 2)
    )

    val field = StructField("value", ArrayType(StringType))

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val generateArrayData = PrivateMethod[GenericArrayData](Symbol("generateArrayData"))

    val result = generator invokePrivate generateArrayData(field, fields("matrix").expression, fields("matrix").count, "matrix")
    val expected = new GenericArrayData(Seq(
      new GenericArrayData(Seq(UTF8String.fromString("1"), UTF8String.fromString("1"))),
      new GenericArrayData(Seq(UTF8String.fromString("1"), UTF8String.fromString("1")))
    ))

    result shouldBe expected

  }

  behavior of "generateValue"

  it should "return partition value when partition is defined" in {
    val generator = new DataGenerator(DummyConfig, DummySchema, DummyFields)

    val field = StructField("field", StringType)
    val partition = Some(Partition("field", UTF8String.fromString("value")))

    val result = generator.generateValue(field, partition)(DummyPrefix)
    result shouldBe UTF8String.fromString("value")
  }

  it should "generate primitive type when dataType is primitive" in {
    val fields = Map(
      "field" -> FieldConfig(expression = "value")
    )

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val arrayField = StructField("field", StringType)

    val result = generator.generateValue(arrayField, None)(DummyPrefix)

    result shouldBe UTF8String.fromString("value")
  }

  it should "generate GenericInternalRow when dataType is StructType" in {
    val fields = Map(
      "struct.field" -> FieldConfig(expression = "value")
    )

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val field = StructField("struct", StructType(Seq(StructField("field", StringType))))

    val result = generator.generateValue(field, None)(DummyPrefix)
    result shouldBe a [GenericInternalRow]
  }

  it should "generate GenericArrayData when dataType is ArrayType" in {
    val fields = Map(
      "array" -> FieldConfig(expression = "value", count = 1)
    )

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val arrayField = StructField("array", ArrayType(StringType))

    val result = generator.generateValue(arrayField, None)(DummyPrefix)

    result shouldBe a [GenericArrayData]
  }

  it should "generate ArrayBasedMapData when dataType is MapType" in {
    val fields = Map(
      "map" -> FieldConfig(key = "key", value = "value", count = 1)
    )

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)
    val arrayField = StructField("map", MapType(StringType, StringType))

    val result = generator.generateValue(arrayField, None)(DummyPrefix)

    result shouldBe a [ArrayBasedMapData]
  }

  behavior of "generateRow"

  it should "return GenericInternalRow" in {
    val fields = Map(
      "struct.field" -> FieldConfig(expression = "value")
    )

    val generator = new DataGenerator(DummyConfig, DummySchema, fields)

    val result = generator.generateRow(None)
    result shouldBe a [GenericInternalRow]
  }

}
