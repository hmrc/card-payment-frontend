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

import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData}
import payapi.corcommon.model.AmountInPence
import play.api.Logging
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.connectors.EmailConnector
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.models.email.{EmailParameters, EmailRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.SucceedWebPaymentRequest
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class EmailService @Inject() (emailConnector: EmailConnector, requestSupport: RequestSupport)(implicit messagesApi: MessagesApi)
  extends Logging {

  import requestSupport._

  def sendEmail(journey: Journey[JourneySpecificData], emailAddress: EmailAddress, isEnglish: Boolean, succeedWebPaymentRequest: SucceedWebPaymentRequest)
    (implicit headerCarrier: HeaderCarrier, request: Request[_]): Future[Unit] = {
    logger.info("Email sent on successful payment")
    emailConnector.sendEmail(
      buildEmailRequest(journey, emailAddress, isEnglish, succeedWebPaymentRequest)
    )(headerCarrier)
  }

  private[services] def buildEmailRequest(
      journey:                  Journey[JourneySpecificData],
      emailAddress:             EmailAddress,
      isEnglish:                Boolean,
      succeedWebPaymentRequest: SucceedWebPaymentRequest
  )(implicit request: Request[_]): EmailRequest = {

    val templateId: String = if (isEnglish) "payment_successful" else "payment_successful_cy"
    val parameters: EmailParameters = buildEmailParameters(journey, succeedWebPaymentRequest)

    EmailRequest(
      to         = List(emailAddress),
      templateId = templateId,
      parameters = parameters,
      force      = false
    )
  }

  private[services] def buildEmailParameters(journey: Journey[JourneySpecificData], succeedWebPaymentRequest: SucceedWebPaymentRequest)(implicit request: Request[_]): EmailParameters = {
    val messages: Messages = request.messages
    val extendedOrigin: ExtendedOrigin = journey.origin.lift
    val maybeCommission = succeedWebPaymentRequest.commissionInPence.map(AmountInPence(_))

    EmailParameters(
      taxType          = messages(extendedOrigin.emailTaxTypeMessageKey),
      taxReference     = obfuscateReference(journey.referenceValue),
      paymentReference = journey.getTransactionReference.value,
      amountPaid       = journey.getAmountInPence.formatInDecimal,
      commission       = if (hasCardFees(maybeCommission)) maybeCommission.map(_.formatInDecimal) else None,
      totalPaid        = if (hasCardFees(maybeCommission)) maybeCommission.map(commission => (journey.getAmountInPence + commission).formatInDecimal) else None
    )
  }

  private[services] def hasCardFees: Option[AmountInPence] => Boolean = maybeAmount => maybeAmount.exists(_.value > 0)

  private[services] def obfuscateReference(taxReference: String)(implicit request: Request[_]): String = {
    request.messages.messages("email.obfuscated-tax-reference", taxReference.takeRight(5))
  }

}
