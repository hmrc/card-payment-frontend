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

package uk.gov.hmrc.cardpaymentfrontend.services

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, Url}
import play.api.i18n.{Lang, Messages, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.connectors.PaymentsSurveyConnector
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.paymentssurvey.{AuditOptions, PaymentSurveyJourneyRequest, SurveyBannerTitle, SurveyContentOptions}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentsSurveyService @Inject() (
  appConfig:               AppConfig,
  requestSupport:          RequestSupport,
  paymentsSurveyConnector: PaymentsSurveyConnector
)(implicit
  executionContext:        ExecutionContext,
  messagesApi:             MessagesApi
) {

  import requestSupport.*

  def startPaySurvey(journey: Journey[JourneySpecificData])(implicit request: Request[?]): Future[Url] = {
    paymentsSurveyConnector
      .startSurvey(makeSsjJourneyRequest(journey))
      .map(_.nextUrl)
  }

  private[services] def makeSsjJourneyRequest(journey: Journey[JourneySpecificData])(implicit request: Request[?]): PaymentSurveyJourneyRequest = {
    implicit val messages: Messages    = request.messages
    val extendedOrigin: ExtendedOrigin = journey.origin.lift(appConfig)

    PaymentSurveyJourneyRequest(
      origin = journey.origin.entryName,
      returnMsg = messages(extendedOrigin.surveyReturnMessageKey),
      returnHref = extendedOrigin.surveyReturnHref,
      auditName = extendedOrigin.surveyAuditName,
      audit = AuditOptions.getAuditOptions(journey, extendedOrigin),
      contentOptions = SurveyContentOptions(
        isWelshSupported = extendedOrigin.surveyIsWelshSupported,
        title = SurveyBannerTitle(
          englishValue = messages(extendedOrigin.surveyBannerTitle),
          welshValue = if (extendedOrigin.surveyIsWelshSupported) Some(messagesApi(extendedOrigin.surveyBannerTitle)(Lang("cy"))) else None
        )
      )
    )
  }
}
