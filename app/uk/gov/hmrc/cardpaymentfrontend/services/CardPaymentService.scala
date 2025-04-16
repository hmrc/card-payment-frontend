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
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.connectors.{CardPaymentConnector, PayApiConnector}
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.{CardPaymentInitiatePaymentRequest, CardPaymentInitiatePaymentResponse, CardPaymentResult, ClientId}
import uk.gov.hmrc.cardpaymentfrontend.models.payapi.{BeginWebPaymentRequest, FailWebPaymentRequest, FinishedWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress, Language}
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CardPaymentService @Inject() (
    appConfig:            AppConfig,
    cardPaymentConnector: CardPaymentConnector,
    payApiConnector:      PayApiConnector,
    clientIdService:      ClientIdService
)(implicit executionContext: ExecutionContext) extends Logging {

  def initiatePayment(
      journey:               Journey[_],
      addressFromSession:    Address,
      maybeEmailFromSession: Option[EmailAddress],
      language:              Language
  )(implicit headerCarrier: HeaderCarrier, requestHeader: RequestHeader): Future[CardPaymentInitiatePaymentResponse] = {

    //todo move this to a val outside the function, then we can test it too
    val urlToComeBackFromAfterIframe: String = uk.gov.hmrc.cardpaymentfrontend.controllers.routes.PaymentStatusController.returnToHmrc().absoluteURL()(requestHeader)

    val clientId: ClientId = clientIdService.determineClientId(journey, language)
    val clientIdStringToUse = if (appConfig.useProductionClientIds) clientId.prodCode else clientId.qaCode

    val cardPaymentInitiatePaymentRequest: CardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest(
      redirectUrl         = urlToComeBackFromAfterIframe,
      clientId            = clientIdStringToUse,
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

  def finishPayment(transactionReference: String, journeyId: String)(implicit headerCarrier: HeaderCarrier): Future[Option[CardPaymentResult]] = {
    logger.info("TransactionReference: " + transactionReference) //todo take me out before go live
    for {
      result <- cardPaymentConnector.authAndSettle(transactionReference)
      cardPaymentResult = result.json.asOpt[CardPaymentResult]
      maybeWebPaymentRequest: Option[FinishedWebPaymentRequest] = cardPaymentResult.flatMap(_.intoUpdateWebPaymentRequest)
      _ <- maybeWebPaymentRequest.fold(payApiConnector.JourneyUpdates.updateCancelWebPayment(journeyId)) {
        case r: FailWebPaymentRequest    => payApiConnector.JourneyUpdates.updateFailWebPayment(journeyId, r)
        case r: SucceedWebPaymentRequest => payApiConnector.JourneyUpdates.updateSucceedWebPayment(journeyId, r)
      }
    } yield cardPaymentResult
  }

}
