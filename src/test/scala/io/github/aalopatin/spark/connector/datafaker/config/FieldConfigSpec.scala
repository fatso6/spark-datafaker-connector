package io.github.aalopatin.spark.connector.datafaker.config

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class FieldConfigSpec extends AnyFlatSpec with Matchers {
  "FieldConfig" should "wrap non-null values into Options via custom apply" in {
    val config = FieldConfig("expr", "key1", "val1", 5)
    config.expression shouldEqual Some("expr")
    config.key shouldEqual Some("key1")
    config.value shouldEqual Some("val1")
    config.count shouldEqual Some(5)
  }

  it should "wrap null values as None via custom apply" in {
    val config = FieldConfig()
    config.expression shouldBe None
    config.key shouldBe None
    config.value shouldBe None
    config.count shouldBe None
  }

  it should "convert to JSON string without null fields" in {
    val config = FieldConfig("expr", "key", "value", null)
    config.toString shouldEqual """{"expression":"expr","key":"key","value":"value"}"""
  }

  it should "implicitly convert to string using custom implicit" in {
    import FieldConfig.implicits._
    val config = FieldConfig("expr", "key", "value", null)
    val result: String = config
    result shouldEqual """{"expression":"expr","key":"key","value":"value"}"""
  }

  it should "throw if only key is set without value" in {
    val ex = intercept[IllegalArgumentException] {
      FieldConfig(key = "keyOnly")
    }
    ex.getMessage should include ("both key and value must be set")
  }

  it should "throw if only value is set without key" in {
    val ex = intercept[IllegalArgumentException] {
      FieldConfig(value = "valueOnly")
    }
    ex.getMessage should include ("both key and value must be set")
  }

  it should "not throw if both key and value are set" in {
    noException should be thrownBy FieldConfig(key = "k", value = "v")
  }

}
