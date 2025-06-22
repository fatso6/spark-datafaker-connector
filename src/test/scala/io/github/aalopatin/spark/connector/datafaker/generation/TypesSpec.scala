package io.github.aalopatin.spark.connector.datafaker.generation

import org.apache.spark.sql.types._
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import java.util.TimeZone


class TypesSpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks with BeforeAndAfterAll {

  override def beforeAll(): Unit = {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  }

  "convertValue" should "handle StringType" in {
    Types.convertValue("hello", StringType) shouldEqual UTF8String.fromString("hello")
  }

  it should "handle ByteType" in {
    Types.convertValue("127", ByteType) shouldEqual 127.toByte
  }

  it should "handle ShortType" in {
    Types.convertValue("32000", ShortType) shouldEqual 32000.toShort
  }

  it should "handle IntegerType" in {
    Types.convertValue("123456", IntegerType) shouldEqual 123456
  }

  it should "handle LongType" in {
    Types.convertValue("1234567890123", LongType) shouldEqual 1234567890123L
  }

  it should "handle FloatType" in {
    Types.convertValue("3.14", FloatType) shouldEqual 3.14f
  }

  it should "handle DoubleType" in {
    Types.convertValue("2.718281828", DoubleType) shouldEqual 2.718281828
  }

  it should "handle BooleanType" in {
    Types.convertValue("true", BooleanType) shouldEqual true
  }


  it should "handle DateType conversions" in {
    val testCases = Table(
      ("input", "expectedEpochDay"),
      ("2025-05-28", 20236),
      ("2025-05-28T12:30:45Z", 20236),
      ("2025-05-28 12:30:45", 20236)
    )

    forAll(testCases) { (input, expected) =>
      Types.convertValue(input, DateType) shouldEqual expected
    }
  }

  it should "handle TimestampType conversions" in {
    val testCases = Table(
      ("input", "expectedMicros"),
      ("2025-05-28", 1_748_390_400_000_000L),
      ("2025-05-28T12:30:45Z", 1_748_435_445_000_000L),
      ("2025-05-28 12:30:45", 1_748_435_445_000_000L)
    )

    forAll(testCases) { (input, expected) =>
      Types.convertValue(input, TimestampType) shouldEqual expected
    }
  }

  it should "throw an exception for unsupported types" in {
    val exception = intercept[IllegalArgumentException] {
      Types.convertValue("test", NullType)
    }

    exception.getMessage should include("Unsupported data type")
  }

  "isSupported" should "return true for supported types" in {
    val supportedTypes = Table(
      "dataType",
      StringType, ByteType, ShortType, IntegerType, LongType,
      FloatType, DoubleType, BooleanType, DateType, TimestampType
    )

    forAll(supportedTypes) { dt =>
      Types.isSupported(dt) shouldBe true
    }
  }

  it should "return false for unsupported types" in {
    val unsupportedTypes = Table(
      "dataType",
      StructType(Seq()), MapType(StringType, StringType), ArrayType(StringType)
    )

    forAll(unsupportedTypes) { dt =>
      Types.isSupported(dt) shouldBe false
    }
  }

  "isComplex" should "return true for complex types" in {
    val complexTypes = Table(
      "dataType",
      StructType(Seq()), MapType(StringType, StringType), ArrayType(StringType)
    )

    forAll(complexTypes) { dt =>
      Types.isComplex(dt) shouldBe true
    }
  }

  it should "return false for simple types" in {
    val simpleTypes = Table(
      "dataType",
      StringType, IntegerType, BooleanType, DateType
    )

    forAll(simpleTypes) { dt =>
      Types.isComplex(dt) shouldBe false
    }
  }

}

//class TypesTest extends AnyFunSuite with TableDrivenPropertyChecks with BeforeAndAfterAll {
//
//  override protected def beforeAll(): Unit = {
//    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
//  }
//
//  test("convertValue handles StringType") {
//    val input = "hello"
//    val expected = UTF8String.fromString("hello")
//    val result = convertValue(input, StringType)
//    assert(result == expected)
//  }
//
//  test("convertValue handles ByteType") {
//    val input = "127"
//    val expected: Byte = 127
//    assert(Types.convertValue(input, ByteType) == expected)
//  }
//
//  test("convertValue handles ShortType") {
//    val input = "32000"
//    val expected: Short = 32000
//    assert(Types.convertValue(input, ShortType) == expected)
//  }
//
//  test("convertValue handles IntegerType") {
//    val input = "123456"
//    val expected = 123456
//    assert(Types.convertValue(input, IntegerType) == expected)
//  }
//
//  test("convertValue handles LongType") {
//    val input = "1234567890123"
//    val expected = 1234567890123L
//    assert(Types.convertValue(input, LongType) == expected)
//  }
//
//  test("convertValue handles FloatType") {
//    val input = "3.14"
//    val expected = 3.14f
//    assert(Types.convertValue(input, FloatType) == expected)
//  }
//
//  test("convertValue handles DoubleType") {
//    val input = "2.718281828"
//    val expected = 2.718281828
//    assert(Types.convertValue(input, DoubleType) == expected)
//  }
//
//  test("convertValue handles BooleanType") {
//    val input = "true"
//    val expected = true
//    assert(Types.convertValue(input, BooleanType) == expected)
//  }
//
//  test("convertValue handles DateType with LocalDate string") {
//    val input = "2025-05-28"
//    val expected = 20236  // correct epoch day for 2025-05-28
//    val result = Types.convertValue(input, DateType)
//    assert(result == expected)
//  }
//
//  test("convertValue handles DateType with Instant string") {
//    val input = "2025-05-28T12:30:45Z"
//    val expected = 20236  // same local date epoch day
//    val result = Types.convertValue(input, DateType)
//    assert(result == expected)
//  }
//
//  test("convertValue handles DateType with Timestamp string") {
//    val input = "2025-05-28 12:30:45"
//    val expected = 20236
//    val result = Types.convertValue(input, DateType)
//    assert(result == expected)
//  }
//
//  test("convertValue handles TimestampType with LocalDate string") {
//    val input = "2025-05-28"
//    val expected = 1_748_390_400_000_000L
//    val result = Types.convertValue(input, TimestampType)
//    assert(result == expected)
//  }
//
//  test("convertValue handles TimestampType with Instant string") {
//    val input = "2025-05-28T12:30:45Z"
//    val expected = 1_748_435_445_000_000L
//    val result = Types.convertValue(input, TimestampType)
//    assert(result == expected)
//  }
//
//  test("convertValue handles TimestampType with Timestamp string") {
//    val input = "2025-05-28 12:30:45"
//    val expected = 1_748_435_445_000_000L
//    val result = Types.convertValue(input, TimestampType)
//    assert(result == expected)
//  }
//
//  test("isSupported returns true for supported types") {
//    val types = Seq(StringType, ByteType, ShortType, IntegerType, LongType, FloatType, DoubleType, BooleanType, DateType, TimestampType)
//    types.foreach { dt =>
//      assert(Types.isSupported(dt))
//    }
//  }
//
//  test("isSupported returns false for unsupported types") {
//    val unsupportedTypes = Seq(StructType(Seq()), MapType(StringType, StringType), ArrayType(StringType))
//    unsupportedTypes.foreach { dt =>
//      assert(!Types.isSupported(dt))
//    }
//  }
//
//  test("isComplexType returns true for complex types") {
//    val complexTypes = Seq(StructType(Seq()), MapType(StringType, StringType), ArrayType(StringType))
//    complexTypes.foreach { dt =>
//      assert(Types.isComplexType(dt))
//    }
//  }
//
//  test("isComplexType returns false for simple types") {
//    val simpleTypes = Seq(StringType, IntegerType, BooleanType, DateType)
//    simpleTypes.foreach { dt =>
//      assert(!Types.isComplexType(dt))
//    }
//  }
//
//  test("convertValue throws for unsupported type") {
//    val exception = intercept[IllegalArgumentException] {
//      Types.convertValue("test", NullType)
//    }
//    assert(exception.getMessage.contains("Unsupported data type"))
//  }
//}