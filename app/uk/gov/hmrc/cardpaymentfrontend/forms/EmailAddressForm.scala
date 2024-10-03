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

import play.api.data.Forms.mapping
import play.api.data.format.Formatter
import play.api.data.{Form, FormError, Forms}
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress

import scala.util.matching.Regex

object EmailAddressForm {

  val emailAddressKey: String = "email-address"

  private val emailAddressRegex: Regex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  def form(): Form[EmailAddress] = {
    val emailAddressMapping = Forms.of(new Formatter[EmailAddress]() {

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], EmailAddress] = {
        //trim first as we allow spaces either side for accessibility reasons.
        data.get(key).map(_.trim) match {
          case Some(email) =>
            if (email.isBlank) Right(EmailAddress(email))
            else if (email.matches(emailAddressRegex.regex)) Right(EmailAddress(email))
            else Left(Seq(FormError(key, "email-address.error.invalid")))
          case None => Right(EmailAddress(""))
        }
      }

      override def unbind(key: String, value: EmailAddress): Map[String, String] = Map(key -> value.value)
    })

    Form(
      mapping = mapping(
        emailAddressKey -> emailAddressMapping
      )(identity)(Some(_))
    )
  }
}
