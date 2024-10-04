/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.cardpaymentfrontend.forms

import play.api.data.{Form, FormError}
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

class AddressFormSpec extends UnitSpec {

  val form: Form[Address] = AddressForm.form()

  "AddressForm" - {

    "should not throw errors when a full valid form is submitted" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "line2" -> "Some Cottage",
        "city" -> "Nice Town",
        "county" -> "Cool County",
        "postcode" -> "AA11 AA",
        "country" -> "GBR"
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }

    "should not throw errors when form is submitted but optional fields omitted" in {

      val validAddress: Map[String, String] = Map(
        "line1" -> "20 Street Road",
        "postcode" -> "AA11 AA",
        "country" -> "GBR"
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
        "postcode" -> "AA11 AA",
        "country" -> "GBR"
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
        "postcode" -> "",
        "country" -> ""
      )

      val result: Form[Address] = form.bind(validAddress)
      result.hasErrors shouldBe true
      result.errors shouldBe List(FormError("line1", List("error.invalid.addressline1")), FormError("country", List("error.required.country")))
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
      result.errors shouldBe List(FormError("line1", List("error.invalid.addressline1"), List()))
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
      result.errors shouldBe List(FormError("postcode", List("error.empty.postcode")))
    }

  }

}
