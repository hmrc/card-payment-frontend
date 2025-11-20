/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.cardpaymentfrontend.forms

import org.scalatest.AppendedClues.convertToClueful
import org.scalatest.Assertion
import play.api.data.format.Formatter
import play.api.data.{Form, FormError}
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

import scala.util.matching.Regex

class AddressFormSpec extends UnitSpec {

  val form: Form[Address] = AddressForm.form()

  private def testForm(data: Map[String, String], expectedErrorList: List[FormError]): Assertion = {
    val result: Form[Address] = form.bind(data)
    result.errors.map(_.key) shouldBe expectedErrorList.map(_.key) withClue s"!! error key wrong, expectedErrors: [ ${expectedErrorList.toString()} ], but was [ ${result.errors.toString()} ]"
    result.errors.map(_.message) shouldBe expectedErrorList.map(_.message) withClue s"!! error message wrong, expectedErrors: [ ${expectedErrorList.toString()} ], but was [ ${result.errors.toString()} ]"
  }

  private def createLongString(limit: Int): String = (1 to limit).map(_ => "a").mkString

  "AddressForm" - {

    "should not throw errors when a full valid form is submitted" in {
      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "Some Cottage",
        "city" -> "Nice Town",
        "county" -> "Cool County",
        "postcode" -> "AA11AA",
        "country" -> "GBR"
      )
      testForm(data              = validAddress, expectedErrorList = List.empty)
    }

    "should not throw errors when form is submitted but optional fields omitted" in {
      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "postcode" -> "AA11AA",
        "country" -> "GBR"
      )
      testForm(data              = validAddress, expectedErrorList = List.empty)
    }

    "should not throw errors when form is submitted but optional fields contain spaces or are empty" in {
      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "  ",
        "city" -> "  ",
        "county" -> "  ",
        "postcode" -> "AA11AA",
        "country" -> "GBR"
      )
      testForm(data              = validAddress, expectedErrorList = List.empty)
    }

    "should trim whitespace from postcode field and return valid address" in {
      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "",
        "city" -> "",
        "county" -> "",
        "postcode" -> " AA1 1AA ",
        "country" -> "GBR"
      )
      val expectedAddress = Address(
        line1    = "20 Street Road",
        line2    = None,
        city     = None,
        county   = None,
        postcode = Some("AA1 1AA"),
        country  = "GBR"
      )
      form.bind(validAddress).value shouldBe Some(expectedAddress)
    }

    "should trim whitespace from line1, line2, city and county fields and return valid address" in {
      val validAddress: Map[String, String] = Map(
        "line1" -> " 20 Street Road ",
        "line2" -> " ",
        "city" -> " ",
        "county" -> " ",
        "postcode" -> " AA1 1AA ",
        "country" -> "GBR"
      )
      val expectedAddress = Address(
        line1    = "20 Street Road",
        line2    = None,
        city     = None,
        county   = None,
        postcode = Some("AA1 1AA"),
        country  = "GBR"
      )
      form.bind(validAddress).value shouldBe Some(expectedAddress)
    }

    "should error" - {

      "for line 1 field" - {

        "when it is empty" in {
          val address = Map(
            "line1" -> "",
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("line1", List("address.field-name.error.line1.empty"))))
        }

        "when it is whitespace" in {
          val address = Map(
            "line1" -> "  ",
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("line1", List("address.field-name.error.line1.empty"))))
        }

        "when entry is more than character limit of 100 characters" in {
          val address = Map(
            "line1" -> createLongString(101),
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("line1", List("address.field-name.error.line1.max-length"))))
        }
      }

      "for line2" - {
        "when entry is more than character limit of 100 characters" in {
          val address = Map(
            "line1" -> "123",
            "line2" -> createLongString(101),
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("line2", List("address.field-name.error.line2.max-length"))))
        }
      }

      "for city" - {
        "when entry is more than character limit of 50 characters" in {
          val address = Map(
            "line1" -> "123",
            "city" -> createLongString(51),
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("city", List("address.field-name.error.city.max-length"))))
        }
      }

      "for county" - {
        "when entry is more than character limit of 50 characters" in {
          val address = Map(
            "line1" -> "123",
            "county" -> createLongString(51),
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("county", List("address.field-name.error.county.max-length"))))
        }
      }

      "for postcode" - {
        "when entry is empty, but country in form is GBR" in {
          val address = Map(
            "line1" -> "123",
            "postcode" -> "",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("postcode", List("address.field-name.error.postcode.empty"))))
        }
      }
    }

    "for country" - {

      "when entry is more than character limit of 3 characters" in {
        val address = Map(
          "line1" -> "123",
          "postcode" -> "AA11AA",
          "country" -> "GBRA"
        )
        testForm(data              = address, expectedErrorList =
          List(FormError("country", List("address.field-name.error.country.invalid-character"))))
      }

      "when entry is not uppercase as per regex" in {
        val address = Map(
          "line1" -> "123",
          "postcode" -> "AA11AA",
          "country" -> "aaa"
        )
        testForm(data              = address, expectedErrorList = List(FormError("country", List("address.field-name.error.country.invalid-character"))))
      }

      "when entry is is not 3 characters as per regex" in {
        val address = Map(
          "line1" -> "123",
          "postcode" -> "AA11AA",
          "country" -> "AA"
        )
        testForm(data              = address, expectedErrorList = List(FormError("country", List("address.field-name.error.country.invalid-character"))))
      }
    }

  }

  "when country is not GBR, errors thrown for line1 only" in {

    val validAddress: Map[String, String] = Map(
      "line1" -> "",
      "line2" -> "Some Cottage",
      "city" -> "Nice Town",
      "county" -> "Cool County",
      "postcode" -> "",
      "country" -> "BMU"
    )

    val result: Form[Address] = form.bind(validAddress)
    result.hasErrors shouldBe true
    result.errors shouldBe List(FormError("line1", List("address.field-name.error.line1.empty")))
  }

  "when country is GBR, errors thrown for a missing postcode" in {

    val validAddress: Map[String, String] = Map(
      "line1" -> "20 Street Road",
      "line2" -> "Some Cottage",
      "city" -> "Nice Town",
      "county" -> "Cool County",
      "postcode" -> "",
      "country" -> "GBR"
    )

    val result: Form[Address] = form.bind(validAddress)
    result.hasErrors shouldBe true
    result.errors shouldBe List(FormError("postcode", List("address.field-name.error.postcode.empty")))
  }

  def testFormatter[T, O](formatter: Formatter[T], inputMap: Map[String, String], expectedOutput: O): Assertion =
    formatter.bind("inputKey", inputMap) shouldBe expectedOutput

  "line1Formatter" - {
    "should return Right[String] when input is valid" in {
      testFormatter[String, Either[Seq[FormError], String]](
        formatter      = AddressForm.line1Formatter,
        inputMap       = Map[String, String]("line1" -> "validAddress"),
        expectedOutput = Right("validAddress")
      )
    }
    "should return Right[String], trimming whitespace either side of input when input is valid" in {
      testFormatter[String, Either[Seq[FormError], String]](
        formatter      = AddressForm.line1Formatter,
        inputMap       = Map[String, String]("line1" -> " valid address "),
        expectedOutput = Right("valid address")
      )
    }
    "should return Left[FormError]" - {
      "indicating empty field when input is empty string" in {
        testFormatter[String, Either[Seq[FormError], String]](
          formatter      = AddressForm.line1Formatter,
          inputMap       = Map[String, String]("line1" -> ""),
          expectedOutput = Left(Seq(FormError("line1", List("address.field-name.error.line1.empty"))))
        )
      }
      "indicating too many characters when input is more than 100 characters" in {
        testFormatter[String, Either[Seq[FormError], String]](
          formatter      = AddressForm.line1Formatter,
          inputMap       = Map[String, String]("line1" -> "a" * 101),
          expectedOutput = Left(Seq(FormError("line1", List("address.field-name.error.line1.max-length"))))
        )
      }
    }
  }

  "line2Formatter" - {
    "should return Right[Some[String]] when input is valid" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.line2Formatter,
        inputMap       = Map[String, String]("line2" -> "validAddress"),
        expectedOutput = Right(Some("validAddress"))
      )
    }
    "should return Right[Some[String]], trimming whitespace either side of input when input is valid" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.line2Formatter,
        inputMap       = Map[String, String]("line2" -> " valid address "),
        expectedOutput = Right(Some("valid address"))
      )
    }
    "should return Right[None] when input is all whitespace" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.line2Formatter,
        inputMap       = Map[String, String]("line2" -> " "),
        expectedOutput = Right(None)
      )
    }
    "should return Left[FormError]" - {
      "indicating too many characters when input is more than 100 characters" in {
        testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
          formatter      = AddressForm.line2Formatter,
          inputMap       = Map[String, String]("line2" -> "a" * 101),
          expectedOutput = Left(Seq(FormError("line2", List("address.field-name.error.line2.max-length"))))
        )
      }
    }
  }

  "cityAndCountyFormatter" - {
    "should return Right[Some[String]] when input is valid" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.cityAndCountyFormatter("city"),
        inputMap       = Map[String, String]("city" -> "validAddress"),
        expectedOutput = Right(Some("validAddress"))
      )
    }
    "should return Right[Some[String]], trimming whitespace either side of input when input is valid" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.cityAndCountyFormatter("city"),
        inputMap       = Map[String, String]("city" -> " valid address "),
        expectedOutput = Right(Some("valid address"))
      )
    }
    "should return Right[None] when input is all whitespace" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.cityAndCountyFormatter("city"),
        inputMap       = Map[String, String]("city" -> " "),
        expectedOutput = Right(None)
      )
    }
    "should return Right[None] when form key doesn't relate to formatter in question" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.cityAndCountyFormatter(formKey = "city1"),
        inputMap       = Map[String, String]("city" -> " "),
        expectedOutput = Right(None)
      )
    }
    "should return Left[FormError]" - {
      "indicating too many characters when input is more than 50 characters" in {
        testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
          formatter      = AddressForm.cityAndCountyFormatter("city"),
          inputMap       = Map[String, String]("city" -> "a" * 51),
          expectedOutput = Left(Seq(FormError("city", List("address.field-name.error.city.max-length"))))
        )
      }
    }
  }

  "postcodeFormatter" - {
    "should return Right[Some[String]] when input is valid and country is GBR" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.postcodeFormatter,
        inputMap       = Map[String, String]("postcode" -> "AA11AA", "country" -> "GBR"),
        expectedOutput = Right(Some("AA11AA"))
      )
    }
    "should return Right[Some[String]], trimming whitespace either side of input when input is valid and country is GBR" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.postcodeFormatter,
        inputMap       = Map[String, String]("postcode" -> " aa1 1aa ", "country" -> "GBR"),
        expectedOutput = Right(Some("aa1 1aa"))
      )
    }
    "should return Right[Some[String]], trimming whitespace either side of input when input is valid and country is not GBR" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.postcodeFormatter,
        inputMap       = Map[String, String]("postcode" -> " aa1 1aa ", "country" -> "SWE"),
        expectedOutput = Right(Some("aa1 1aa"))
      )
    }
    "should return Right[None] postcode is empty and country is not GBR" in {
      testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
        formatter      = AddressForm.postcodeFormatter,
        inputMap       = Map[String, String]("postcode" -> " ", "country" -> "SWE"),
        expectedOutput = Right(None)
      )
    }

    "should return Left[FormError]" - {
      "indicating postcode is required when input is empty and country is GBR" in {
        testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
          formatter      = AddressForm.postcodeFormatter,
          inputMap       = Map[String, String]("postcode" -> "", "country" -> "GBR"),
          expectedOutput = Left(Seq(FormError("postcode", List("address.field-name.error.postcode.empty"))))
        )
      }
      "indicating postcode contains invalid character when input doesn't match UK postcode regex and country is GBR" in {
        testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
          formatter      = AddressForm.postcodeFormatter,
          inputMap       = Map[String, String]("postcode" -> "ABCDE!", "country" -> "GBR"),
          expectedOutput = Left(Seq(FormError("postcode", List("address.field-name.error.postcode.invalid-character"))))
        )
      }
      "indicating postcode contains invalid character when input doesn't match barclaycard postcode regex and country is not GBR (i.e. more than 16 characters)" in {
        testFormatter[Option[String], Either[Seq[FormError], Option[String]]](
          formatter      = AddressForm.postcodeFormatter,
          inputMap       = Map[String, String]("postcode" -> "ABCDEFGHIJKLMNOPQ", "country" -> "SWE"),
          expectedOutput = Left(Seq(FormError("postcode", List("address.field-name.error.postcode.invalid-character"))))
        )
      }
    }

  }

  "AddressForm regexes" - {

    "addressUkPostcodeRegex" - {
      val regex: Regex = AddressForm.addressUkPostcodeRegex

      Seq("BN1 2TL", "BN12TL", "EC1A 1BB", "ZE1 0TF", "M1 1BE", "JE2 3QA", "bn12tl").foreach { postcode =>
        s"should match a valid postcode: $postcode" in {
          regex.matches(postcode) shouldEqual true
        }
      }

      Seq("A1", "12 412", "", """\\s""", " BN1 2TL ", "GH FJU", "gh fju").foreach { postcode =>
        s"should not match an invalid postcode: $postcode" in {
          regex.matches(postcode) shouldEqual false
        }
      }
    }
  }

}
