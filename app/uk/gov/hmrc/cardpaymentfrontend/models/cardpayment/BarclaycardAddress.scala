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

package uk.gov.hmrc.cardpaymentfrontend.models.cardpayment

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps

//barclaycard want field names in a specific way, so we have another case class for it to send to the backend
final case class BarclaycardAddress(
    line1:       String,
    line2:       Option[String] = None,
    city:        Option[String] = None,
    county:      Option[String] = None,
    postCode:    Option[String] = None,
    countryCode: String
) {

  require(
    requirement = {
      if (countryCode === "GBR") postCode.isDefined
      else true
    },
    message     = "If country code is GBR (United Kingdom) we expect a postcode to be defined."
  )

  def hasSelect(maybeString: Option[String]): Boolean = {
    maybeString match {
      case Some(s) =>
        val selectPattern = "(?i)select".r
        selectPattern.findFirstIn(s).isDefined
      case None => false
    }
  }

  // For UK (GBR) addresses only, replace counties containing variations on Select with None.
  def sanitiseCounty(): BarclaycardAddress =
    if (countryCode.matches("GBR") && hasSelect(this.county)) this.copy (county = None)
    else this
}

@SuppressWarnings(Array("org.wartremover.warts.Any"))
object BarclaycardAddress {
  implicit val format: Format[BarclaycardAddress] = Json.format[BarclaycardAddress]
}
