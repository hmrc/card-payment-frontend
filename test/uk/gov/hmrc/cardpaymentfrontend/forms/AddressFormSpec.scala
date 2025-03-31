/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.data.{Form, FormError}
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

import scala.collection.immutable.ArraySeq
import scala.util.matching.Regex

class AddressFormSpec extends UnitSpec {

  val form: Form[Address] = AddressForm.form()

  "AddressForm" - {

    "should not throw errors when a full valid form is submitted" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "Some Cottage",
        "city" -> "Nice Town",
        "county" -> "Cool County",
        "postCode" -> "AA11 AA",
        "countryCode" -> "GBR"
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }

    "should not throw errors when form is submitted but optional fields omitted" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "postCode" -> "AA11 AA",
        "countryCode" -> "GBR"
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }

    "should not throw errors when form is submitted but optional fields contain spaces or are empty" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "   ",
        "city" -> "",
        "county" -> "",
        "postCode" -> "AA11 AA",
        "countryCode" -> "GBR"
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }

    "When country is empty, errors thrown for line1 and country only" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "",
        "line2" -> "Some Cottage",
        "city" -> "Nice Town",
        "county" -> "Cool County",
        "postCode" -> "",
        "countryCode" -> ""
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(FormError("line1", List("address.field-name.error.invalid.line1")), FormError("countryCode", List("address.field-name.error.required.countryCode")))
    }

    "When country is not GBR, errors thrown for line1 only" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "",
        "line2" -> "Some Cottage",
        "city" -> "Nice Town",
        "county" -> "Cool County",
        "postCode" -> "",
        "countryCode" -> "BMU"
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(FormError("line1", List("address.field-name.error.invalid.line1"), List()))
    }

    "When country is GBR, errors thrown for a missing postcode" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "Some Cottage",
        "city" -> "Nice Town",
        "county" -> "Cool County",
        "postCode" -> "",
        "countryCode" -> "GBR"
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(FormError("postCode", List("address.field-name.error.empty.postCode")))
    }

    "Throw errors when fields are too long" in {

        def createLongString(limit: Int): String = (1 to limit).map(_ => "a").mkString

      val invalidAddress: Map[String, String] = Map(
        "line1" -> createLongString(51),
        "line2" -> createLongString(51),
        "city" -> createLongString(61),
        "county" -> createLongString(61),
        "postCode" -> "AA11 AA",
        "countryCode" -> "GBR"
      )

      val result: Form[Address] = form.bind(invalidAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(
        FormError("line1", List("error.maxLength"), ArraySeq(50)),
        FormError("line2", List("error.maxLength"), ArraySeq(50)),
        FormError("city", List("error.maxLength"), ArraySeq(60)),
        FormError("county", List("error.maxLength"), ArraySeq(60))
      )
    }

    "Should not accept emojis" in {

      val invalidAddress: Map[String, String] = Map(
        "line1" -> "🍗",
        "line2" -> "🐸",
        "city" -> "🍄",
        "county" -> "🦕",
        "postCode" -> "AA11 AA",
        "countryCode" -> "GBR"
      )

      val result: Form[Address] = form.bind(invalidAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(
        FormError("line1", List("address.field-name.error.invalid.char")),
        FormError("line2", List("address.field-name.error.invalid.char")),
        FormError("city", List("address.field-name.error.invalid.char")),
        FormError("county", List("address.field-name.error.invalid.char"))
      )
    }

  }

  "AddressForm.ukPostcodeRegex" - {

    val regex: Regex = AddressForm.ukPostcodeRegex

    Seq("BN1 2TL", "BN12TL", "EC1A 1BB", "ZE1 0TF", "M1 1BE", "JE2 3QA").foreach{ postcode =>
      s"should match a valid postcode: $postcode" in {
        regex.matches(postcode) shouldEqual true
      }
    }

    Seq("A1", "12 412", "", """\\s""", " BN1 2TL ", "GH FJU").foreach{ postcode =>
      s"should not match an invalid postcode: $postcode" in {
        regex.matches(postcode) shouldEqual false
      }
    }

  }

}
