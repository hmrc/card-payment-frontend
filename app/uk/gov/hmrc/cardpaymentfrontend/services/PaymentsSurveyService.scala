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

package uk.gov.hmrc.cardpaymentfrontend.services

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, Url}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.connectors.PaymentsSurveyConnector
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey.{AuditOptions, PaymentSurveyJourneyRequest, SurveyBannerTitle, SurveyContentOptions}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.utils.OriginExtraInfo

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsSurveyService @Inject() (
    requestSupport:          RequestSupport,
    originExtraInfo:         OriginExtraInfo,
    paymentsSurveyConnector: PaymentsSurveyConnector
)(
    implicit
    executionContext: ExecutionContext, messagesApi: MessagesApi
) {

  import requestSupport._

  def startPaySurvey(journey: Journey[JourneySpecificData])(implicit request: Request[_]): Future[Url] = {
    val paymentSurveyJourneyRequest: PaymentSurveyJourneyRequest = makeSsjJourneyRequest(journey)
    paymentsSurveyConnector
      .startSurvey(paymentSurveyJourneyRequest)
      .map(_.nextUrl)
  }

  private[services] def makeSsjJourneyRequest(journey: Journey[JourneySpecificData])(implicit request: Request[_]): PaymentSurveyJourneyRequest = {
    implicit val messages: Messages = request.messages
    val extendedOrigin: ExtendedOrigin = originExtraInfo.lift(journey.origin)
    val origin: String = journey.origin.entryName
    val returnMessage: String = messages(extendedOrigin.surveyReturnMessageKey)
    val returnHref: String = extendedOrigin.surveyReturnHref
    val auditName: String = extendedOrigin.surveyAuditName
    val auditOptions: AuditOptions = AuditOptions.getAuditOptions(journey, extendedOrigin)
    val contentItems: SurveyContentOptions = SurveyContentOptions(
      isWelshSupported = extendedOrigin.surveyIsWelshSupported,
      title            = SurveyBannerTitle(
        englishValue = messages(extendedOrigin.surveyBannerTitle),
        welshValue   = Some(messagesApi(extendedOrigin.surveyBannerTitle)(Lang("cy")))
      )
    )

    PaymentSurveyJourneyRequest(
      origin         = origin,
      returnMsg      = returnMessage,
      returnHref     = returnHref,
      auditName      = auditName,
      audit          = auditOptions,
      contentOptions = contentItems
    )
  }
}
