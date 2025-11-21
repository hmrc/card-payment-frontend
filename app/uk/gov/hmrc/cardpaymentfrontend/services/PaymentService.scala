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
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.connectors.PayApiConnector
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PaymentService @Inject() (payApiConnector: PayApiConnector)(implicit executionContext: ExecutionContext) {

  def resetSentJourney()(implicit request: JourneyRequest[_]): Future[Unit] =
    request.journey.order.fold[Future[Unit]](Future.successful(()))(_ => resetWebPayment())

  private def resetWebPayment()(implicit journeyRequest: JourneyRequest[_]): Future[Unit] = {
    for {
      _ <- payApiConnector.JourneyUpdates.resetWebPayment(journeyRequest.journey._id.value)
    } yield ()
  }

}
