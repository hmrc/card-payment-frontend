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

package uk.gov.hmrc.cardpaymentfrontend.models.notifications

import play.api.libs.json.{Json, OWrites}

final case class CdsNotification(notifyImmediatePaymentRequest: NotifyImmediatePaymentRequest)

object CdsNotification {
  implicit val writes: OWrites[CdsNotification] = Json.writes[CdsNotification]
}

final case class NotifyImmediatePaymentRequest(requestCommon: RequestCommon, requestDetail: RequestDetail)

object NotifyImmediatePaymentRequest {
  implicit val writes: OWrites[NotifyImmediatePaymentRequest] = Json.writes[NotifyImmediatePaymentRequest]
}

final case class RequestCommon(receiptDate: String, acknowledgementReference: String, regime: String = "CDS", originatingSystem: String = "OPS")

object RequestCommon {
  implicit val writes: OWrites[RequestCommon] = Json.writes[RequestCommon]
}

final case class RequestDetail(paymentReference: String, amountPaid: String, unitType: String = "GBP", declarationID: String)

object RequestDetail {
  implicit val writes: OWrites[RequestDetail] = Json.writes[RequestDetail]
}
