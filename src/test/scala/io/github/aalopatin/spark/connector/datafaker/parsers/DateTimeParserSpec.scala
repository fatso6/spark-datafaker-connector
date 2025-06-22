package io.github.aalopatin.spark.connector.datafaker.parsers

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.prop.TableDrivenPropertyChecks

import java.sql.Timestamp
import java.time._

class DateTimeParserSpec extends AnyFlatSpec with Matchers with TableDrivenPropertyChecks {

  "parse" should "parse a string in yyyy-MM-dd format into a LocalDate" in {
    val input = "2025-05-28"
    val expected = LocalDate.of(2025, 5, 28)
    DateTimeParser.parse(input) shouldEqual expected
  }


  it should "correctly parse Instant strings with various fractional seconds" in {
    val testCases = Table(
      "input",
      "2025-05-28T12:30:45Z",
      "2025-05-28T12:30:45.1Z",
      "2025-05-28T12:30:45.123Z",
      "2025-05-28T12:30:45.123456Z",
      "2025-05-28T12:30:45.123456789Z"
    )

    forAll(testCases) { input =>
      val expected = Instant.parse(input)
      DateTimeParser.parse(input) shouldEqual expected
    }
  }

  it should "correctly parse Timestamp strings with various fractional seconds" in {
    val testCases = Table(
      ("input", "expected"),
      ("2025-05-28 12:30:45", Timestamp.valueOf("2025-05-28 12:30:45")),
      ("2025-05-28 12:30:45.1", Timestamp.valueOf("2025-05-28 12:30:45.1")),
      ("2025-05-28 12:30:45.123", Timestamp.valueOf("2025-05-28 12:30:45.123")),
      ("2025-05-28 12:30:45.123456", Timestamp.valueOf("2025-05-28 12:30:45.123456")),
      ("2025-05-28 12:30:45.123456789", Timestamp.valueOf("2025-05-28 12:30:45.123456789"))
    )

    forAll(testCases) { (input, expected) =>
      DateTimeParser.parse(input) shouldEqual expected
    }
  }

  it should "throw IllegalArgumentException for invalid date strings" in {
    val invalidInput = "not-a-date"
    val ex = intercept[IllegalArgumentException] {
      DateTimeParser.parse(invalidInput)
    }
    ex.getMessage should include ("Unrecognized date/time format")
  }

}
