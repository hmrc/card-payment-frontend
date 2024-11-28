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

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.connectors.EmailConnector
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.utils.OriginExtraInfo
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector, originExtraInfo: OriginExtraInfo, requestSupport: RequestSupport)(implicit messagesApi: MessagesApi) {

  import requestSupport._

  //TODO: Mike: call this function from payment success controller. Eventually we can call it from the payment-status controller, but we don't have that yet.
  def sendEmail(journey: Journey[JourneySpecificData], isEnglish: Boolean)(implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Unit] = {
    val emailRequest = buildEmailRequest(journey, isEnglish)
    emailConnector.sendEmail(emailRequest)(headerCarrier) // TODO: This should be found implicitly
  }

  private[services] def buildEmailRequest(
      journey:   Journey[JourneySpecificData],
      isEnglish: Boolean
  )(implicit request: Request[_]): EmailRequest = {
    val templateId = if (isEnglish) "payment_successful" else "payment_successful_cy"
    val parameters: EmailParameters = EmailParameters("Self Assessment", "1234567895K", "Some-transaction-ref", "12.34", None, Some("12.34"))
    //    val parameters = buildEmailParameters(journey)
    //    val maybeEmailAddress = request.readFromSession[String](journey._id, "email").map(_.toString) // TODO: Do we need an Email model?

    EmailRequest(
      to         = List("joe_bloggs@gmail.com"), // TODO: will need to pass this in from the session?
      templateId = templateId,
      parameters = parameters,
      force      = false
    )
  }

  private[services] def buildEmailParameters(journey: Journey[JourneySpecificData])(implicit request: Request[_]): EmailParameters = {
    val messages: Messages = request.messages
    val extendedOrigin: ExtendedOrigin = originExtraInfo.lift(journey.origin)
    EmailParameters(
      taxType          = messages(extendedOrigin.emailTaxTypeMessageKey),
      taxReference     = journey.referenceValue,
      paymentReference = journey.getTransactionReference.value,
      amountPaid       = journey.getAmountInPence.formatInDecimal,
      commission       = journey.getCommissionInPence.map(_.formatInDecimal),
      totalPaid        = Some(journey.getTotalAmountInPence.formatInDecimal)
    )
  }

}
