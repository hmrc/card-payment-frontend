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

import play.api.data.Forms.{mapping, text}
import play.api.data.format.Formatter
import play.api.data.validation.Constraints.pattern
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps

import scala.util.matching.Regex

object AddressForm {

  private[forms] val addressLineRegex: Regex = "^[\\s\\S]{1,100}$".r
  private[forms] val addressCityAndCountyRegex: Regex = "^[\\s\\S]{1,50}$".r
  private[forms] val addressUkPostcodeRegex: Regex = "([Gg][Ii][Rr] 0[Aa]{2})|((([A-Za-z][0-9]{1,2})|(([A-Za-z][A-Ha-hJ-Yj-y][0-9]{1,2})|(([A-Za-z][0-9][A-Za-z])|([A-Za-z][A-Ha-hJ-Yj-y][0-9]?[A-Za-z]))))\\s?[0-9][A-Za-z]{2})".r
  private[forms] val addressBarclaycardPostcodeRegex: Regex = "^[\\s\\S]{1,16}$".r
  private[forms] val addressCountryRegex: Regex = "^[A-Z]{3}$".r

  def form(): Form[Address] = Form(
    mapping(
      "line1" -> Forms.of(line1Formatter),
      "line2" -> Forms.of(line2Formatter),
      "city" -> Forms.of(cityAndCountyFormatter("city")),
      "county" -> Forms.of(cityAndCountyFormatter("county")),
      "postcode" -> Forms.of(postcodeFormatter),
      "country" -> text.verifying(pattern(addressCountryRegex, error = "address.field-name.error.country.invalid-character"))

    )(Address.apply)(Address.unapply)
  )

  private[forms] def line1Formatter: Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], String] = {
      val line1Key = "line1"
      val line1: String = data(line1Key).trim

      if (line1.isBlank) {
        Left(Seq(FormError(line1Key, "address.field-name.error.line1.empty")))
      } else if (!line1.matches(addressLineRegex.regex))
        // regex maps to max length error, as barclaycard spec regex allows anything but spaces, as long as it's within 1-100 characters
        Left(Seq(FormError(line1Key, "address.field-name.error.line1.max-length")))
      else
        Right(line1)
    }
    override def unbind(key: String, value: String): Map[String, String] = Map(key -> value)
  }

  private[forms] def line2Formatter: Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val line2Key: String = "line2"
      val maybeLine2: Option[String] = data.get(line2Key).map(_.trim)

      maybeLine2.fold[Either[Seq[FormError], Option[String]]](Right(None)) { line2 =>
        if (line2.forall(_.isWhitespace))
          Right(None)
        else if (!line2.matches(addressLineRegex.regex))
          // regex maps to max length error, as barclaycard spec regex allows anything but spaces, as long as it's within 1-100 characters
          Left(Seq(FormError(line2Key, "address.field-name.error.line2.max-length")))
        else
          Right(Some(line2))
      }
    }

    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  private[forms] def cityAndCountyFormatter(formKey: String): Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val maybeFormData: Option[String] = data.get(formKey).map(_.trim)

      maybeFormData.fold[Either[Seq[FormError], Option[String]]](Right(None)) { formData =>
        if (formData.forall(_.isWhitespace))
          Right(None)
        else if (!formData.matches(addressCityAndCountyRegex.regex)) {
          // regex maps to max length error, as barclaycard spec regex allows anything but spaces, as long as it's within 1-100 characters
          Left(Seq(FormError(formKey, s"address.field-name.error.$formKey.max-length")))
        } else Right(Some(formData))
      }
    }
    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }

  private[forms] def postcodeFormatter: Formatter[Option[String]] = new Formatter[Option[String]] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Option[String]] = {
      val maybePostcode: Option[String] = data.get("postcode").map(_.trim)
      val selectedCountryIsGBR: Boolean = data.get("country") === Some("GBR")

      if (selectedCountryIsGBR && maybePostcode.exists(_.forall(_.isWhitespace)))
        Left(Seq(FormError("postcode", "address.field-name.error.postcode.empty")))
      else if (selectedCountryIsGBR && (!maybePostcode.exists(_.matches(addressUkPostcodeRegex.regex)) || !maybePostcode.exists(_.matches(addressBarclaycardPostcodeRegex.regex))))
        Left(Seq(FormError("postcode", "address.field-name.error.postcode.invalid-character")))
      else if (!selectedCountryIsGBR && maybePostcode.exists(_.forall(_.isWhitespace)))
        Right(None)
      else if (!selectedCountryIsGBR && !maybePostcode.exists(_.matches(addressBarclaycardPostcodeRegex.regex)))
        Left(Seq(FormError("postcode", "address.field-name.error.postcode.invalid-character")))
      else
        Right(maybePostcode)
    }
    override def unbind(key: String, value: Option[String]): Map[String, String] = Map(key -> value.getOrElse(""))
  }
}
