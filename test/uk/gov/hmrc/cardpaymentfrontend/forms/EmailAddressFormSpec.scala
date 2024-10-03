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
}
