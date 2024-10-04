/*
 * Copyright 2024 HM Revenue & Customs
 *
 */

package uk.gov.hmrc.cardpaymentfrontend.forms

import play.api.data.Form
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



    "should not throw errors when valid form submitted but with spaces either side" in {
      val result: Form[Address] = form.bind(Map("email-address" -> "  blah@blah.com  "))
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }
  }

}
