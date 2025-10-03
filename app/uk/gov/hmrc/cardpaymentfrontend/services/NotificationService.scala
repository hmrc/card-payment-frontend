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
import play.api.Logging
import uk.gov.hmrc.cardpaymentfrontend.connectors.CdsConnector
import uk.gov.hmrc.cardpaymentfrontend.models.notifications.{CdsNotification, NotifyImmediatePaymentRequest, RequestCommon, RequestDetail}
import uk.gov.hmrc.http.HeaderCarrier

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDateTime, ZoneId}
import java.util.Locale
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

@Singleton
class NotificationService @Inject() (cdsConnector: CdsConnector, clock: Clock)(implicit executionContext: ExecutionContext) extends Logging {

  @SuppressWarnings(Array("org.wartremover.warts.AsInstanceOf"))
  def sendNotification(journey: Journey[JourneySpecificData])(implicit headerCarrier: HeaderCarrier): Unit = {
    val _ = Try {
      journey.journeySpecificData match {
        case _: JsdPfCds  => sendCdsNotification(journey.asInstanceOf[Journey[JsdPfCds]])
        case _: JsdBcPngr => noOperation(journey)
        case _: JsdMib    => noOperation(journey)
        case _            => noOperation(journey)
      }
    }.recover {
      case e => logger.error(s"[Problem sending card-payment-notification] [journeyId: ${journey._id.value}] [origin: ${journey.origin.toString}]", e)
    }
  }

  /**
   * Inform that you haven't sent notification.
   */
  private def noOperation(journey: Journey[JourneySpecificData]): Unit = {
    logger.info(s"Not sending any card-payment-notification [${journey.origin.toString}] [${journey.status.toString}] [${journey._id.toString}]")
  }

  private[services] def sendCdsNotification(journey: Journey[JsdPfCds])(implicit headerCarrier: HeaderCarrier): Unit = {
    val notificationF: Future[Unit] = for {
      cdsSubscriptionDetails <- cdsConnector.getCashDepositSubscriptionDetails(journey.journeySpecificData.cdsRef.getOrElse(throw new RuntimeException("CDS reference missing for notification, this should never happen")))
      cdsNotification = buildCdsNotification(journey, LocalDateTime.now(clock), cdsSubscriptionDetails.getCashDepositSubscriptionDetailsResponse.responseDetail.declarationID)
      _ <- cdsConnector.sendNotification(cdsNotification)(journey.getTransactionReference)
    } yield logger.info(s"[CdsNotification] [DONE] [journeyId: ${journey._id.value}] successfully sent card-payment-notification")

    val _ = notificationF.recover {
      case e => logger.error(s"[CdsNotification] [Problem sending card-payment-notification] [journeyId: ${journey._id.value}]", e)
    }
  }

  private[services] def buildCdsNotification(journey: Journey[JsdPfCds], eventDateTime: LocalDateTime, declarationId: String): CdsNotification = {
    CdsNotification(
      notifyImmediatePaymentRequest = NotifyImmediatePaymentRequest(
        requestCommon = RequestCommon(
          receiptDate              = eventDateTime.atZone(ZoneId.of("UTC")).format(cdsEventTimeFormat),
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

  private val cdsEventTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.UK)

}
