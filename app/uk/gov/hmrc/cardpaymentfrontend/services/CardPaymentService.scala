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
import payapi.corcommon.model.{JourneyId, Origins, PaymentStatuses, TransNumberGenerator}
import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.connectors.{CardPaymentConnector, PayApiConnector}
import uk.gov.hmrc.cardpaymentfrontend.logging.KibanaLogger
import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.*
import uk.gov.hmrc.cardpaymentfrontend.models.payapirequest.{BeginWebPaymentRequest, FailWebPaymentRequest, FinishedWebPaymentRequest, SucceedWebPaymentRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress, Language}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport.{Keys, RequestOps}
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import java.time.{Clock, LocalDateTime}
import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

@Singleton
class CardPaymentService @Inject() (
  appConfig:            AppConfig,
  auditService:         AuditService,
  cardPaymentConnector: CardPaymentConnector,
  clientIdService:      ClientIdService,
  clock:                Clock,
  cryptoService:        CryptoService,
  emailService:         EmailService,
  notificationService:  NotificationService,
  payApiConnector:      PayApiConnector,
  requestSupport:       RequestSupport,
  transNoGenerator:     TransNumberGenerator
)(implicit executionContext: ExecutionContext)
    extends Logging {

  import requestSupport.*

  // the url barclaycard make an empty post to after user completes payment
  private[services] def returnToHmrcUrl(journeyId: JourneyId): String = {
    // we encrypt the trace id and put it in the return url as a path parameter so we can find the journey when user comes back from barclaycard.
    val encryptedJourneyId: String              = cryptoService.encryptString(journeyId.value)
    // we base64 encode the encrypted journeyId so it passes barclaycard regex for urls.
    val base64EncodedEncryptedJourneyId: String = Base64.getUrlEncoder.encodeToString(encryptedJourneyId.getBytes)
    s"${appConfig.cardPaymentFrontendBaseUrl}${uk.gov.hmrc.cardpaymentfrontend.controllers.routes.PaymentStatusController.returnToHmrc(base64EncodedEncryptedJourneyId).url}"
  }

  def initiatePayment(
    journey:               Journey[?],
    addressFromSession:    Address,
    maybeEmailFromSession: Option[EmailAddress],
    language:              Language
  )(implicit headerCarrier: HeaderCarrier, journeyRequest: JourneyRequest[?]): Future[CardPaymentInitiatePaymentResponse] = {
    val clientId: ClientId          = clientIdService.determineClientId(journey, language)
    val clientIdStringToUse: String = if (appConfig.useProductionClientIds) clientId.prodCode else clientId.qaCode
    KibanaLogger.info(s"Initiating payment for journey ${journey._id.value}")

    // todo eventually we can just use barclaycardaddress model, but we want backwards compatibility with pay-frontend.
    // This way if user ends up on pay-frontend after being on card-payment-frontend (or vice versa) it won't break.
    val decryptedAddressFromSession: Address                       = cryptoService.decryptAddress(addressFromSession)
    val addressFromSessionAsBarclaycardAddress: BarclaycardAddress = BarclaycardAddress(
      line1 = decryptedAddressFromSession.line1,
      line2 = decryptedAddressFromSession.line2,
      city = decryptedAddressFromSession.city,
      county = decryptedAddressFromSession.county,
      postCode = decryptedAddressFromSession.postcode,
      countryCode = decryptedAddressFromSession.country
    )

    val cardPaymentInitiatePaymentRequest: CardPaymentInitiatePaymentRequest = CardPaymentInitiatePaymentRequest(
      redirectUrl = returnToHmrcUrl(journey._id),
      clientId = clientIdStringToUse,
      purchaseDescription = journey.referenceValue,
      purchaseAmount = journey.getAmountInPence,
      billingAddress = addressFromSessionAsBarclaycardAddress,
      emailAddress = maybeEmailFromSession.map(cryptoService.decryptEmail), // we decrypt here as barclaycard needs the email in plain text.
      transactionNumber = transNoGenerator.generate(journey.origin)
    )

    for {
      initiatePaymentResponse     <- cardPaymentConnector.initiatePayment(cardPaymentInitiatePaymentRequest)(using headerCarrier)
      _                            = auditService.auditPaymentAttempt(addressFromSession, clientIdStringToUse, initiatePaymentResponse.transactionReference)(journeyRequest, headerCarrier)
      payApiBeginWebPaymentRequest = BeginWebPaymentRequest(initiatePaymentResponse.transactionReference, initiatePaymentResponse.redirectUrl)
      _                           <- payApiConnector.JourneyUpdates.updateBeginWebPayment(journey._id.value, payApiBeginWebPaymentRequest)(using
                                       headerCarrier
                                     ) // should we enhance this and error/retry?
    } yield {
      KibanaLogger.info(s"Payment initiated for journey ${journey._id.value}.")
      initiatePaymentResponse
    }
  }

  def finishPayment(
    transactionReference: String,
    journeyId:            String,
    language:             Language
  )(implicit journeyRequest: JourneyRequest[?], messagesApi: MessagesApi): Future[Option[CardPaymentResult]] = {
    val clientId: ClientId = clientIdService.determineClientId(journeyRequest.journey, language)

    KibanaLogger.info(s"Finishing payment for journey $journeyId.")

    val maybeCardPaymentResultF: Future[Option[CardPaymentResult]] = for {
      result                                                   <- cardPaymentConnector.authAndSettle(transactionReference)
      cardPaymentResult                                         = result.json.asOpt[CardPaymentResult]
      _                                                         = cardPaymentResult.map { result =>
                                                                    auditService.auditPaymentResult(
                                                                      if (appConfig.useProductionClientIds) clientId.prodCode else clientId.qaCode,
                                                                      transactionReference,
                                                                      result.cardPaymentResult.toString
                                                                    )
                                                                  }
      maybeWebPaymentRequest: Option[FinishedWebPaymentRequest] = cardPaymentResult.flatMap(cardPaymentResultIntoUpdateWebPaymentRequest)
      _                                                        <- maybeWebPaymentRequest.fold {
                                                                    KibanaLogger.info(s"Payment cancelled for journey $journeyId.")
                                                                    payApiConnector.JourneyUpdates
                                                                      .updateCancelWebPayment(journeyId)
                                                                      .map[Option[FinishedWebPaymentRequest]](_ => None)
                                                                  } {
                                                                    case r: FailWebPaymentRequest    =>
                                                                      KibanaLogger.info(s"Payment failed for journey $journeyId.")
                                                                      payApiConnector.JourneyUpdates
                                                                        .updateFailWebPayment(journeyId, r)
                                                                        .map[Option[FinishedWebPaymentRequest]](_ => Some(r))
                                                                    case r: SucceedWebPaymentRequest =>
                                                                      KibanaLogger.info(s"Payment finished for journey $journeyId.")
                                                                      payApiConnector.JourneyUpdates
                                                                        .updateSucceedWebPayment(journeyId, r)
                                                                        .map[Option[FinishedWebPaymentRequest]](_ => Some(r))
                                                                  }
    } yield cardPaymentResult

    maybeCardPaymentResultF.andThen { case Success(_) =>
      postPaymentResultOperations(JourneyId(journeyId))
    }
  }

  def cancelPayment()(implicit journeyRequest: JourneyRequest[?]): Future[HttpResponse] = {
    val transactionReference = journeyRequest.journey.getTransactionReference
    val clientId             = clientIdService.determineClientId(journeyRequest.journey, requestSupport.usableLanguage)
    val clientIdStringToUse  = if (appConfig.useProductionClientIds) clientId.prodCode else clientId.qaCode

    for {
      cancelPaymentHttpResponse <- cardPaymentConnector.cancelPayment(transactionReference.value, clientIdStringToUse)
      _                         <- payApiConnector.JourneyUpdates.updateCancelWebPayment(journeyRequest.journey._id.value)
    } yield cancelPaymentHttpResponse

  }

  private[services] def cardPaymentResultIntoUpdateWebPaymentRequest: CardPaymentResult => Option[FinishedWebPaymentRequest] = {
    case CardPaymentResult(CardPaymentFinishPaymentResponses.Successful, additionalPaymentInfo) =>
      Some(
        SucceedWebPaymentRequest(
          additionalPaymentInfo.cardCategory.getOrElse("debit"),
          additionalPaymentInfo.commissionInPence,
          additionalPaymentInfo.transactionTime.getOrElse(LocalDateTime.now(clock))
        )
      )
    case CardPaymentResult(CardPaymentFinishPaymentResponses.Failed, additionalPaymentInfo)     =>
      Some(
        FailWebPaymentRequest(
          additionalPaymentInfo.transactionTime.getOrElse(LocalDateTime.now(clock)),
          additionalPaymentInfo.cardCategory.getOrElse("debit") // todo check if can this be anything?
        )
      )
    case CardPaymentResult(CardPaymentFinishPaymentResponses.Cancelled, _)                      => None
  }

  private[services] def postPaymentResultOperations(journeyId: JourneyId)(implicit request: Request[?], messagesApi: MessagesApi): Future[Unit] = {
    for {
      // fetch the latest incarnation of the journey, with up to date info
      latestJourney <- payApiConnector.findJourneyByJourneyId(journeyId)
      journey        = latestJourney.getOrElse(throw new RuntimeException("No journey found, we cannot progress with post payment operations!"))
      _              = maybeSendEmailF(journey)
      _              = notificationService.sendNotification(journey)
    } yield ()
  }

  /** If journey is Successful, origin is not Mib or BcPngr, and they have an email in session, send an email. Otherwise, return future.unit.
    */
  private[services] def maybeSendEmailF(
    journey: Journey[JourneySpecificData]
  )(implicit headerCarrier: HeaderCarrier, request: Request[?], messagesApi: MessagesApi): Unit = {
    if (journey.origin === Origins.Mib || journey.origin === Origins.BcPngr) {
      logger.debug(s"Not sending email for ${journey.origin.entryName}")
    } else {
      if (journey.status === PaymentStatuses.Successful) {
        val maybeEmailFromSession: Option[EmailAddress] =
          request
            .readFromSession[EmailAddress](journey._id, Keys.email)
            .map(cryptoService.decryptEmail)
            .filter(e => !e.value.isBlank)

        logger.debug("Attempting to build email request and send email")

        maybeEmailFromSession.fold(()) { emailAddress =>
          emailService
            .sendEmail(
              journey = journey,
              emailAddress = emailAddress,
              isEnglish = request.lang.code =!= "cy"
            )(headerCarrier, request)
            .onComplete(_ => ())
        }
      }
    }
  }

}
