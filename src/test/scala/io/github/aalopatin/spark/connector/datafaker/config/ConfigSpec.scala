package io.github.aalopatin.spark.connector.datafaker.config

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers


class ConfigSpec extends AnyFlatSpec with Matchers {

  "Config" should "initialize with default values" in {
    val config = Config()
    config.silentCheck shouldBe true
    config.locale shouldEqual "en"
    config.numRows shouldEqual 10
  }

  it should "initialize with custom values" in {
    val config = Config(silentCheck = false, locale = "it", numRows = 42)
    config.silentCheck shouldBe false
    config.locale shouldEqual "it"
    config.numRows shouldEqual 42
  }

  it should "throw IllegalArgumentException for non-positive numRows" in {
    val exception = intercept[IllegalArgumentException] {
      Config(numRows = 0)
    }
    exception.getMessage shouldEqual "requirement failed: numRows must be positive"
  }
}
