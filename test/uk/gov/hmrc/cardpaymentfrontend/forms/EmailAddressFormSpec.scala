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

import org.scalatest.prop.TableDrivenPropertyChecks
import play.api.data.{Form, FormError}
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

import scala.util.matching.Regex

class EmailAddressFormSpec extends UnitSpec with TableDrivenPropertyChecks {

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

    "should accept valid email addresses" in {
      val validTestCases = Table(
        ("abc@abc.com", "abc@abc.com"),
        ("% escaped mail route to user@example.com via example.org", "user%example.com@example.org"),
        ("top level domain", "example@s.example"),
        ("hyphens", "long.email-address-with-hyphens@and.subdomains.example.com"),
        ("may be routed to user.name@example.com inbox depending on mail server", "user.name+tag+sorting@example.com"),
        ("casing", "FirstName.LastName@EasierReading.org"),
        ("special characters", "abc.!#$%&'*+/=?^_`{|}~-@abc.com"),
        ("one letter local", "x@example.com"),
        ("dash at end of local", "user-@example.org")
      )
      forAll(validTestCases) { (testScenario, input) =>
        regex.matches(input) shouldBe true withClue s"failed for $testScenario"
      }
    }

    "should reject invalid email addresses" in {
      val invalidTestCases = Table(
        ("test scenario", "invalid email input"),
        // other
        ("empty string", ""),
        ("whitespace", "\t "),
        ("more than one @ symbol", "abc@abc@abc.com"),
        ("missing @", "abc.example.com"),
        ("multiple @", "a@b@c@example.com"),
        ("spaces, quotes, and backslashes may only exist when within quoted strings and preceded by a backslash", "this is\"not\\allowed@example.com"),
        ("even if escaped backslashes must be contained by quotes", "this\\ still\\\"not\\\\allowed@example.com"),
        ("emoji", "👻blah@blah.com"),
        // local part of email
        ("space within local", "john smith@abc.com"),
        ("dash at start of local", "-abc@abc!.com"),
        ("more than 64 characters for local", s"${"a" * 65}@abc.com"),
        ("full stop at start", "jane.@doe.com"),
        ("full stop at end", ".jane@doe.com"),
        ("consecutive full stops", "jane..doe@abc.com"),
        // domain part of email
        ("missing . in domain", "a@a"),
        ("invalid character in domain", "abc@abc!.com"),
        ("underscore in domain", "i.like.underscores@but_they_are_not_allowed_in_this_part"),
        ("special characters in domain, not in local", "a\"b(c)d,e:f;g<h>i[j\\k]l@example.com")
      )
      forAll(invalidTestCases) { (testScenario, input) =>
        regex.matches(input) shouldBe false withClue s"failed for $testScenario"
      }
    }
  }
}
