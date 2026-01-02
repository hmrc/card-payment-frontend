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
import payapi.corcommon.model.PaymentStatuses
import play.api.Logging
import play.api.mvc.Result
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.connectors.PayApiConnector
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentService @Inject() (payApiConnector: PayApiConnector)(implicit executionContext: ExecutionContext) extends Logging {

  def resetSentJourney()(implicit journeyRequest: JourneyRequest[_]): Future[Unit] =
    journeyRequest.journey.order.fold[Future[Unit]](Future.successful(()))(_ => resetWebPayment())

  def resetSentJourneyThenResult(r: => Result)(implicit journeyRequest: JourneyRequest[_]): Future[Result] =
    journeyRequest.journey.order.fold[Future[Result]](Future.successful(r))(_ => resetWebPayment().map(_ => r))

  // Creates a new journey but copies over stuff like session id and journey specific data.
  // To be used when journeys are cancelled/failed.
  def createCopyOfCancelledOrFailedJourney()(implicit journeyRequest: JourneyRequest[_]): Future[Unit] = {
    journeyRequest.journey.status match {
      case ps @ (PaymentStatuses.Created | PaymentStatuses.Successful | PaymentStatuses.Sent | PaymentStatuses.Validated | PaymentStatuses.SoftDecline) =>
        logger.warn(s"User trying to create deep copy of journey that is in state [${ps.entryName}], when it should only be used by Failed or Cancelled.")
        Future.successful(())

      case ps @ (PaymentStatuses.Cancelled | PaymentStatuses.Failed) =>
        logger.info(s"Cloning journey as user wants to try again since status is ${ps.entryName} for journeyId ${journeyRequest.journeyId.value}")
        payApiConnector.restartJourneyAsNew(journeyRequest.journeyId).map(_ => ())
    }

  }

  private def resetWebPayment()(implicit journeyRequest: JourneyRequest[_]): Future[Unit] = {
    for {
      _ <- payApiConnector.JourneyUpdates.resetWebPayment(journeyRequest.journey._id.value)
    } yield ()
  }

}
