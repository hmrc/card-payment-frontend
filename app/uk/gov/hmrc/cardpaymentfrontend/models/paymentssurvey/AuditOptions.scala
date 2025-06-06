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

package uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport

final case class AuditOptions(
    userType:  String,
    journey:   Option[String] = None,
    orderId:   Option[String] = None,
    liability: Option[String] = None
)

object AuditOptions {

  @SuppressWarnings(Array("org.wartremover.warts.Any"))
  implicit val format: OFormat[AuditOptions] = Json.format[AuditOptions]

  def default(implicit r: Request[_]): AuditOptions = AuditOptions(
    userType = if (RequestSupport.isLoggedIn) "LoggedIn" else "LoggedOut"
  )

  def getAuditOptions(journey: Journey[JourneySpecificData], extendedOrigin: ExtendedOrigin)(implicit r: Request[_]): AuditOptions = {
    AuditOptions(
      userType  = if (RequestSupport.isLoggedIn) "LoggedIn" else "LoggedOut",
      journey   = Option(journey.status.entryName),
      orderId   = journey.reference.map(_.value),
      liability = Some(extendedOrigin.surveyAuditName)
    )
  }

}
