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

import payapi.corcommon.model.AmountInPence
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}

final case class CardPaymentInitiatePaymentRequest(
    redirectUrl:         String,
    clientId:            String,
    purchaseDescription: String, // i.e. tax reference
    purchaseAmount:      AmountInPence, //in pennies
    billingAddress:      Address,
    emailAddress:        Option[EmailAddress]
)

object CardPaymentInitiatePaymentRequest {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: Format[CardPaymentInitiatePaymentRequest] = Json.format[CardPaymentInitiatePaymentRequest]
}
