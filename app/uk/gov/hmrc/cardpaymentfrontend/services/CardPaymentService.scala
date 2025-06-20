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
import payapi.corcommon.model.TransNumberGenerator
import play.api.Logging
import play.api.libs.json.JsBoolean
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.connectors.{CardPaymentConnector, PayApiConnector}
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment._
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.{BeginWebPaymentRequest, FailWebPaymentRequest, FinishedWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress, Language}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.{Clock, LocalDateTime}
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CardPaymentService @Inject() (
    appConfig:            AppConfig,
    cardPaymentConnector: CardPaymentConnector,
    clock:                Clock,
    payApiConnector:      PayApiConnector,
    transNoGenerator:     TransNumberGenerator,
    clientIdService:      ClientIdService
)(implicit executionContext: ExecutionContext) extends Logging {

  // the url barclaycard make an empty post to after user completes payment
  private def returnToHmrcUrl: String =
    s"${appConfig.cardPaymentFrontendBaseUrl}${uk.gov.hmrc.cardpaymentfrontend.controllers.routes.PaymentStatusController.returnToHmrc().url}"

  def initiatePayment(
      journey:               Journey[_],
      addressFromSession:    Address,
      maybeEmailFromSession: Option[EmailAddress],
      language:              Language
  )(implicit headerCarrier: HeaderCarrier): Future[CardPaymentInitiatePaymentResponse] = {
    val clientId: ClientId = clientIdService.determineClientId(journey, language)
    val clientIdStringToUse = if (appConfig.useProductionClientIds) clientId.prodCode else clientId.qaCode

    //todo eventually we can just use barclaycardaddress model, but we want backwards compatibility with pay-frontend.
    // This way if user ends up on pay-frontend after being on card-payment-frontend (or vice versa) it won't break.
    val addressFromSessionAsBarclaycardAddress: BarclaycardAddress = BarclaycardAddress(
      line1       = addressFromSession.line1,
      line2       = addressFromSession.line2,
      city        = addressFromSession.city,
      county      = addressFromSession.county,
      postCode    = addressFromSession.postcode,
      countryCode = addressFromSession.country
    )

    val cardPaymentInitiatePaymentRequest: CardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest(
      redirectUrl         = returnToHmrcUrl,
      clientId            = clientIdStringToUse,
      purchaseDescription = journey.referenceValue,
      purchaseAmount      = journey.getAmountInPence,
      billingAddress      = addressFromSessionAsBarclaycardAddress,
      emailAddress        = maybeEmailFromSession,
      transactionNumber   = transNoGenerator.generate(journey.origin)
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
    for {
      result <- cardPaymentConnector.authAndSettle(transactionReference)
      cardPaymentResult = result.json.asOpt[CardPaymentResult]
      maybeWebPaymentRequest: Option[FinishedWebPaymentRequest] = cardPaymentResult.flatMap(cardPaymentResultIntoUpdateWebPaymentRequest)
      _ <- maybeWebPaymentRequest.fold(payApiConnector.JourneyUpdates.updateCancelWebPayment(journeyId)) {
        case r: FailWebPaymentRequest    => payApiConnector.JourneyUpdates.updateFailWebPayment(journeyId, r)
        case r: SucceedWebPaymentRequest => payApiConnector.JourneyUpdates.updateSucceedWebPayment(journeyId, r)
      }
    } yield cardPaymentResult
  }

  private[services] def cardPaymentResultIntoUpdateWebPaymentRequest: CardPaymentResult => Option[FinishedWebPaymentRequest] = {
    case CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, additionalPaymentInfo) =>
      Some(SucceedWebPaymentRequest(
        additionalPaymentInfo.cardCategory.getOrElse("debit"),
        additionalPaymentInfo.commissionInPence,
        additionalPaymentInfo.transactionTime.getOrElse(LocalDateTime.now(clock)),
      ))
    case CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, additionalPaymentInfo) =>
      Some(FailWebPaymentRequest(
        additionalPaymentInfo.transactionTime.getOrElse(LocalDateTime.now(clock)),
        additionalPaymentInfo.cardCategory.getOrElse("debit") //todo check if can this be anything?
      ))
    case CardPaymentResult(CardPaymentFinishPaymentResponses.Cancelled, _) => None
  }

}
