package io.github.aalopatin.spark.connector.datafaker.parsers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

class JsonStringParserSpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {

  behavior of "parseArray"

  it should "correctly parse a JSON array of strings" in {
    val json = """["apple", "banana", "cherry"]"""
    JsonStringParser.parseArray(json) shouldBe Seq("apple", "banana", "cherry")
  }

  it should "throw an exception if the input is not a JSON array" in {
    val json = """{"fruit": "apple"}"""
    val ex = intercept[IllegalArgumentException] {
      JsonStringParser.parseArray(json)
    }
    ex.getMessage should include("Expected JSON array")
  }

  it should "throw an exception for invalid JSON" in {
    val json = """["unclosed string]"""
    val ex = intercept[IllegalArgumentException] {
      JsonStringParser.parseArray(json)
    }
    ex.getMessage should include("Invalid JSON")
  }

  behavior of "parseMap"

  it should "correctly parse a JSON object into a Map" in {
    val json = """{"key1": "value1", "key2": "value2"}"""
    JsonStringParser.parseMap(json) shouldBe Map("key1" -> "value1", "key2" -> "value2")
  }

  it should "throw an exception for invalid JSON" in {
    val json = """{"key1": "value1", "key2": }"""
    val ex = intercept[IllegalArgumentException] {
      JsonStringParser.parseMap(json)
    }
    ex.getMessage should include("Failed to parse Map")
  }

  it should "throw an exception for non-map JSON structure" in {
    val json = """["not", "a", "map"]"""
    val ex = intercept[IllegalArgumentException] {
      JsonStringParser.parseMap(json)
    }
    ex.getMessage should include("Failed to parse Map")
  }
}
