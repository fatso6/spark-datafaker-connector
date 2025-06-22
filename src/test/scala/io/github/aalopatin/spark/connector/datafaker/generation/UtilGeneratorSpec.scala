package io.github.aalopatin.spark.connector.datafaker.generation

import org.apache.spark.sql.catalyst.util.GenericArrayData
import org.apache.spark.sql.types.StringType
import org.apache.spark.unsafe.types.UTF8String
import org.scalatest.PrivateMethodTester
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class UtilGeneratorSpec extends AnyFlatSpec with Matchers with PrivateMethodTester {

  behavior of "generateArrayDataFromStrings"

  it should "convert sequence to GenericArrayData" in {
    val inputs = Seq("1", "2", "3")
    val result = UtilGenerator.generateArrayDataFromStrings(inputs, StringType)
    val expected = new GenericArrayData(Seq(UTF8String.fromString("1"), UTF8String.fromString("2"), UTF8String.fromString("3")))
    result shouldBe expected
  }

}
