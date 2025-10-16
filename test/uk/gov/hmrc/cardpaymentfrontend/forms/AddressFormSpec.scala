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

        "when entry does not match regex" in {
          val address = Map(
            "line1" -> "🍗",
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("line1", List("address.field-name.error.line1.invalid-character"))))
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

        "when entry does not match regex" in {
          val address = Map(
            "line1" -> "123",
            "line2" -> "🍗",
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("line2", List("address.field-name.error.line2.invalid-character"))))
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

        "when entry does not match regex" in {
          val address = Map(
            "line1" -> "123",
            "city" -> "🍗",
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("city", List("address.field-name.error.city.invalid-character"))))
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

        "when entry does not match regex" in {
          val address = Map(
            "line1" -> "123",
            "county" -> "🍗",
            "postcode" -> "AA11AA",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("county", List("address.field-name.error.county.invalid-character"))))
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

        "when entry does not match uk postcode regex, but country in form is GBR" in {
          val address = Map(
            "line1" -> "123",
            "postcode" -> "🍗",
            "country" -> "GBR"
          )
          testForm(data              = address, expectedErrorList = List(FormError("postcode", List("address.field-name.error.postcode.invalid-character"))))
        }

        "when entry does not match barclaycard postcode regex and country in form is not GBR" in {
          val address = Map(
            "line1" -> "123",
            "postcode" -> "🍗",
            "country" -> "USA"
          )
          testForm(data              = address, expectedErrorList = List(FormError("postcode", List("address.field-name.error.postcode.invalid-character"))))
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

    "When country is not GBR, errors thrown for line1 only" in {

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

    "When country is GBR, errors thrown for a missing postcode" in {

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

    "Should not accept emojis" in {

      val invalidAddress: Map[String, String] = Map(
        "line1" -> "🍗",
        "line2" -> "🐸",
        "city" -> "🍄",
        "county" -> "🦕",
        "postcode" -> "😀",
        "country" -> "GBR"
      )

      val result: Form[Address] = form.bind(invalidAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(
        FormError("line1", List("address.field-name.error.line1.invalid-character")),
        FormError("line2", List("address.field-name.error.line2.invalid-character")),
        FormError("city", List("address.field-name.error.city.invalid-character")),
        FormError("county", List("address.field-name.error.county.invalid-character")),
        FormError("postcode", List("address.field-name.error.postcode.invalid-character"))
      )
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
