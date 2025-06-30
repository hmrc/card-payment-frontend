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

package uk.gov.hmrc.cardpaymentfrontend.models.audit

import payapi.corcommon.model.{Origin, TaxType}
import play.api.libs.json.{Json, OWrites}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}

final case class PaymentResultAuditDetail(
    address:              Option[Address],
    emailAddress:         Option[EmailAddress],
    loggedIn:             Boolean,
    merchantCode:         String,
    paymentOrigin:        Origin,
    paymentStatus:        String,
    paymentReference:     String,
    paymentTaxType:       TaxType,
    paymentTotal:         BigDecimal,
    transactionReference: String
) extends AuditDetail {
  override val auditType: String = "PaymentResult"
}

object PaymentResultAuditDetail {
  implicit val writes: OWrites[PaymentResultAuditDetail] = Json.writes[PaymentResultAuditDetail]
}
