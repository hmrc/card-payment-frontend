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
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

import scala.util.matching.Regex

class EmailAddressFormSpec extends UnitSpec {

  val form: Form[EmailAddress] = EmailAddressForm.form()

  "EmailAddressForm" - {
    "should not throw errors when valid form submitted" in {
      val result: Form[EmailAddress] = form.bind(Map("email-address" -> "blah@blah.com"))
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }
    "should not throw errors when valid form submitted but with spaces either side" in {
      val result: Form[EmailAddress] = form.bind(Map("email-address" -> "  blah@blah.com  "))
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }
    "should not throw errors when empty form submitted" in {
      val result: Form[EmailAddress] = form.bind(Map("email-address" -> ""))
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }
    "should not throw errors when just spaces submitted" in {
      val result: Form[EmailAddress] = form.bind(Map("email-address" -> "  "))
      result.hasErrors shouldBe false
      result.errors shouldBe List.empty[String]
    }
    "should throw error when an invalid email is entered" in {
      val result: Form[EmailAddress] = form.bind(Map("email-address" -> "not_a_valid_email"))
      result.hasErrors shouldBe true
      result.errors shouldBe List(FormError("email-address", List("email-address.error.invalid")))
    }
  }

  s"EmailAddressForm.emailAddressRegex" - {

    val regex: Regex = EmailAddressForm.emailAddressRegex

    "should match a standard email address" in {
      regex.matches("john.smith@ordinaryemail.com") shouldEqual true
    }
    "should reject an email address with an internal space" in {
      regex.matches("john smith@ordinaryemail.com") shouldEqual false
    }
    "should reject an email address with nothing but whitespace" in {
      regex.matches("\n\t   \r   ") shouldEqual false
    }
    "should reject an email address with the empty string" in {
      regex.matches("") shouldEqual false
    }
    "should reject an email where there is no @ symbol" in {
      regex.matches("johnsmithATordinaryemail.com") shouldEqual false
    }
    "should reject an email where there is more than one @" in {
      regex.matches("johnsmith@abc@abc.com") shouldEqual false
    }
    "should reject an email where domain contains non alphanumerics" in {
      regex.matches("johnsmith@!ordinaryemail.com") shouldEqual false
    }
    "should reject an email where there is no domain" in {
      regex.matches("johnsmith@") shouldEqual false
    }
    "should reject an email where part of the domain is more than 63 characters" in {
      val sixtyFourCharacters: String = (1 to 64).toList.map(_ => "a").mkString
      regex.matches(s"johnsmith@$sixtyFourCharacters.com") shouldEqual false
    }
  }
}
