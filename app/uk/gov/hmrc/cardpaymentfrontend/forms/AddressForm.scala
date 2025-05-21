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

import play.api.data.Forms.{mapping, of, optional, text}
import play.api.data.format.Formatter
import play.api.data.validation.Constraints.maxLength
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.data.{Form, FormError}
import uk.gov.hmrc.cardpaymentfrontend.models.Address

import scala.util.matching.Regex

object AddressForm {

  val addressKey: String = "address"

  private[forms] val ukPostcodeRegex: Regex = "([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z]))))\\s?[0-9][A-Za-z]{2})".r

  def form(): Form[Address] = Form(
    mapping(

      "line1" -> text.transform[String](_.trim, identity).verifying("address.field-name.error.invalid.line1", s => s.nonEmpty)
        .verifying(maxLength(50))
        .verifying(emojiConstraint("line1", "address.field-name.error.invalid.char")),
      "line2" -> optional(text.transform[String](_.trim, identity)
        .verifying(maxLength(50))
        .verifying(emojiConstraint("line2", "address.field-name.error.invalid.char"))),
      "city" -> optional(text.transform[String](_.trim, identity)
        .verifying(maxLength(60))
        .verifying(emojiConstraint("city", "address.field-name.error.invalid.char"))),
      "county" -> optional(text.transform[String](_.trim, identity)
        .verifying(maxLength(60))
        .verifying(emojiConstraint("county", "address.field-name.error.invalid.char"))),
      "postcode" -> of(postcodeFormatter),
      "country" -> text.verifying(countryConstraint)
    )(Address.apply)(Address.unapply)
  )

  def emojiConstraint(name: String, error: String): Constraint[String] = Constraint[String](name) { o =>

    if (containsEmoji(o)) Invalid(ValidationError(error))
    else Valid

  }

  def containsEmoji(valueToCheck: String): Boolean = {

    val regex: String = "[^\\p{L}\\p{N}\\p{P}\\p{Z}\\p{M}\\£$^<>|]"
    val replaced: String = valueToCheck.replaceAll(regex, "")
    if (replaced.matches(valueToCheck)) false else true

  }

  def countryConstraint: Constraint[String] = Constraint[String]("constraint.country") { o =>
    if (o.isBlank) Invalid(ValidationError("address.field-name.error.required.country")) else if (o.trim.isEmpty) Invalid(ValidationError("address.field-name.error.required.country")) else Valid
  }

  val postcodeFormatter: Formatter[String] = new Formatter[String] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {

      // for accessibility, we need to allow users to enter spaces anywhere in postcode, we strip them to assert the postcode matches the regex, then use what the user entered.
      val postcode: String = data("postcode").filterNot(_.isWhitespace)
      val selectedCountryIsGBR: Boolean = data("country").matches("GBR")
      if (selectedCountryIsGBR && postcode.isEmpty)
        Left(Seq(FormError("postcode", "address.field-name.error.empty.postcode")))
      else if (selectedCountryIsGBR && !postcode.matches(ukPostcodeRegex.regex))
        Left(Seq(FormError("postcode", "address.field-name.error.invalid.postcode")))
      else
        Right(postcode)
    }

    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)

  }

}
