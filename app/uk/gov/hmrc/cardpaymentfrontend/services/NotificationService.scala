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

import com.google.inject.{Inject, Singleton}
import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.PaymentStatuses
import play.api.Logging
import play.api.libs.json.Json
import uk.gov.hmrc.cardpaymentfrontend.connectors.{CdsConnector, PassengersConnector, PaymentsProcessorConnector}
import uk.gov.hmrc.cardpaymentfrontend.models.notifications._
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class NotificationService @Inject() (
    cdsConnector:               CdsConnector,
    clock:                      Clock,
    passengersConnector:        PassengersConnector,
    paymentsProcessorConnector: PaymentsProcessorConnector
)(implicit executionContext: ExecutionContext) extends Logging {

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def sendNotification(journey: Journey[JourneySpecificData])(implicit headerCarrier: HeaderCarrier): Unit = {
    val _ = Try {
      journey.journeySpecificData match {
        case _: JsdPfCds  => sendCdsNotification(journey.asInstanceOf[Journey[JsdPfCds]])
        case _: JsdBcPngr => sendPassengersNotification(journey.asInstanceOf[Journey[JsdBcPngr]])
        case _: JsdMib    => sendModsNotification(journey.asInstanceOf[Journey[JsdMib]])
        case _            => noOperation(journey)
      }
    }.recover {
      case e => logger.error(s"[Problem sending card-payment-notification] [journeyId: ${journey._id.value}] [origin: ${journey.origin.toString}]", e)
    }
  }

  private[services] def sendCdsNotification(journey: Journey[JsdPfCds])(implicit headerCarrier: HeaderCarrier): Unit = {
    if (journey.status === PaymentStatuses.Successful) {
      val notificationF: Future[Unit] = for {
        cdsSubscriptionDetails <- cdsConnector.getCashDepositSubscriptionDetails(journey.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("CDS reference missing for notification, this should never happen")))
        cdsNotification = buildCdsNotification(journey, LocalDateTime.now(clock), cdsSubscriptionDetails.getCashDepositSubscriptionDetailsResponse.responseDetail.declarationID)
        _ <- cdsConnector.sendNotification(cdsNotification)(journey.getTransactionReference)
      } yield logSuccessfulNotification(journey)("CdsNotification")

      val _ = notificationF.recover {
        case e => logErrorSendingNotification(journey)("CdsNotification", e)
      }
    } else {
      noOperation(journey, s"[journey status was: ${journey.status.entryName}]")
    }
  }

  private[services] def sendModsNotification(journey: Journey[JsdMib])(implicit headerCarrier: HeaderCarrier): Unit = {
    if (journey.status === PaymentStatuses.Successful) {
      val modsNotification: ModsNotification = buildModsNotification(journey)
      val notificationF: Future[Unit] = for {
        _ <- paymentsProcessorConnector.sendModsNotification(modsNotification)
      } yield logSuccessfulNotification(journey)("ModsNotification")

      val _ = notificationF.recover {
        case e => logErrorSendingNotification(journey)("ModsNotification", e)
      }
    } else {
      noOperation(journey, s"[journey status was: ${journey.status.entryName}]")
    }
  }

  private[services] def sendPassengersNotification(journey: Journey[JsdBcPngr])(implicit headerCarrier: HeaderCarrier): Unit = {
    if (journey.status.isTerminalState) {
      val passengersNotification = buildPassengersNotification(journey, LocalDateTime.now(clock))
      val notificationF: Future[Unit] = for {
        _ <- passengersConnector.sendNotification(passengersNotification)
      } yield logSuccessfulNotification(journey)("PassengersNotification")

      val _ = notificationF.recover {
        case e => logErrorSendingNotification(journey)("PassengersNotification", e)
      }
    } else {
      noOperation(journey, s"[journey was not in terminal state: ${journey.status.entryName}]")
    }
  }

  private[services] def buildCdsNotification(journey: Journey[JsdPfCds], eventDateTime: LocalDateTime, declarationId: String): CdsNotification = {
    CdsNotification(
      notifyImmediatePaymentRequest = NotifyImmediatePaymentRequest(
        requestCommon = RequestCommon(
          receiptDate              = eventDateTime.atZone(ZoneId.of("UTC")).format(CdsNotification.cdsEventTimeFormat),
          acknowledgementReference = journey.order.getOrElse(throw new RuntimeException(s"Expected defined order [${journey.toString}]")).transactionReference.value.replaceAll("-", "")
        ),
        requestDetail = RequestDetail(
          paymentReference = journey.reference.getOrElse(throw new RuntimeException(s"Expected defined reference [${journey.toString}]")).value,
          amountPaid       = journey.amountInPence.getOrElse(throw new RuntimeException(s"Expected defined amountInPence [${journey.toString}]")).inPounds.toString(),
          declarationID    = declarationId
        )
      )
    )
  }

  private[services] def buildModsNotification(journey: Journey[JsdMib]): ModsNotification =
    ModsNotification(journey.journeySpecificData.mibReference, journey.journeySpecificData.amendmentReference)

  private[services] def buildPassengersNotification(journey: Journey[JsdBcPngr], eventDateTime: LocalDateTime): PassengersNotification =
    PassengersNotification(
      paymentId            = journey._id.value,
      taxType              = journey.taxType.toString,
      status               = journey.status,
      amountInPence        = journey.amountInPence.getOrElse(throw new RuntimeException(s"Expected defined amountInPence [${journey.toString}]")).value,
      commissionInPence    = journey.order.flatMap(_.commissionInPence.map(_.value)).getOrElse(0L),
      reference            = journey.reference.getOrElse(throw new RuntimeException(s"Expected defined reference [${journey.toString}]")).value,
      transactionReference = journey.order.getOrElse(throw new RuntimeException(s"Expected defined order [${journey.toString}]")).transactionReference.value,
      notificationData     = Json.obj(), // was empty in pay-frontend, is it even used?
      eventDateTime        = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(eventDateTime)
    )

  /**
   * Inform that you haven't sent notification.
   */
  private def noOperation(journey: Journey[JourneySpecificData], extraInfo: String = ""): Unit = {
    logger.info(s"Not sending any card-payment-notification $extraInfo [${journey.origin.toString}] [${journey.status.toString}] [${journey._id.toString}]")
  }

  private def logErrorSendingNotification(journey: Journey[_])(notificationType: String, throwable: => Throwable): Unit =
    logger.error(s"[$notificationType] [Problem sending card-payment-notification] [journeyId: ${journey._id.value}][\n${journey.toString}\n]", throwable)

  private def logSuccessfulNotification(journey: Journey[_])(notificationType: String): Unit =
    logger.info(s"[$notificationType] [DONE] [journeyId: ${journey._id.value}] successfully sent card-payment-notification")

}
