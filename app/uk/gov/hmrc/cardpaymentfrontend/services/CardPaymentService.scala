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

import payapi.cardpaymentjourney.model.journey.Journey
import play.api.Logging
import play.api.libs.json.JsBoolean
import play.api.mvc.RequestHeader
import uk.gov.hmrc.cardpaymentfrontend.connectors.{CardPaymentConnector, PayApiConnector}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.{CardPaymentInitiatePaymentRequest, CardPaymentInitiatePaymentResponse}
import uk.gov.hmrc.cardpaymentfrontend.models.payapi.BeginWebPaymentRequest
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CardPaymentService @Inject() (cardPaymentConnector: CardPaymentConnector, payApiConnector: PayApiConnector)(implicit executionContext: ExecutionContext) extends Logging {

  def initiatePayment(
      journey:               Journey[_],
      addressFromSession:    Address,
      maybeEmailFromSession: Option[EmailAddress]
  )(implicit headerCarrier: HeaderCarrier, requestHeader: RequestHeader): Future[CardPaymentInitiatePaymentResponse] = {

    val urlToComeBackFromAfterIframe: String = uk.gov.hmrc.cardpaymentfrontend.controllers.routes.PaymentStatusController.returnToHmrc(journey.traceId.value).absoluteURL()(requestHeader)

    val cardPaymentInitiatePaymentRequest: CardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest(
      redirectUrl         = urlToComeBackFromAfterIframe,
      clientId            = "MBEE", //todo need a function that determines this based on tt/origin
      purchaseDescription = journey.referenceValue,
      purchaseAmount      = journey.getAmountInPence,
      billingAddress      = addressFromSession,
      emailAddress        = maybeEmailFromSession
    )

    for {
      initiatePaymentResponse <- cardPaymentConnector.initiatePayment(cardPaymentInitiatePaymentRequest)
      payApiBeginWebPaymentRequest = BeginWebPaymentRequest(initiatePaymentResponse.transactionReference, initiatePaymentResponse.redirectUrl)
      _ <- payApiConnector.JourneyUpdates.updateBeginWebPayment(journey._id.value, payApiBeginWebPaymentRequest) //should we enhance this and error/retry?
    } yield initiatePaymentResponse
  }

  def checkPaymentStatus(transactionReference: String)(implicit headerCarrier: HeaderCarrier): Future[JsBoolean] = {
    cardPaymentConnector.checkPaymentStatus(transactionReference)
  }

  def authAndSettle(transactionReference: String)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    logger.info("TransactionReference: " + transactionReference) //todo take me out before go live
    cardPaymentConnector.authAndSettle(transactionReference)
  }

}
