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

package uk.gov.hmrc.cardpaymentfrontend.models.cds

import play.api.libs.json.{Json, OFormat}

final case class CdsResponse(getCashDepositSubscriptionDetailsResponse: GetCashDepositSubscriptionDetailsResponse)

object CdsResponse {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[CdsResponse] = Json.format[CdsResponse]
}

final case class GetCashDepositSubscriptionDetailsResponse(responseCommon: ResponseCommon, responseDetail: ResponseDetail)

object GetCashDepositSubscriptionDetailsResponse {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[GetCashDepositSubscriptionDetailsResponse] = Json.format[GetCashDepositSubscriptionDetailsResponse]
}

final case class ResponseCommon(status: String, processingDate: String)

object ResponseCommon {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[ResponseCommon] = Json.format[ResponseCommon]
}

final case class ResponseDetail(
  declarationID:              String,
  paymentReference:           String,
  paymentReferenceDate:       String,
  isPaymentReferenceActive:   Boolean,
  paymentReferenceCancelDate: Option[String] = None
)

object ResponseDetail {
  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[ResponseDetail] = Json.format[ResponseDetail]
}
