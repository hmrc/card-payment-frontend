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

import payapi.corcommon.model.PaymentStatus
import play.api.libs.json.{JsObject, Json, OWrites}

final case class PassengersNotification(
    paymentId:            String,
    taxType:              String,
    status:               PaymentStatus,
    amountInPence:        Long,
    commissionInPence:    Long,
    reference:            String,
    transactionReference: String,
    notificationData:     JsObject,
    eventDateTime:        String
)

object PassengersNotification {
  implicit val format: OWrites[PassengersNotification] = Json.writes[PassengersNotification]
}
