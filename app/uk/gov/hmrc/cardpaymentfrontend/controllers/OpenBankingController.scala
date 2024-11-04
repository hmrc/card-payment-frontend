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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import play.api.mvc.Results.Redirect
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.connectors.OpenBankingConnector
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.CreateSessionDataRequest
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.util.OpenBankingUtils

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class OpenBankingController @Inject() (
    actions:              Actions,
    openBankingConnector: OpenBankingConnector,
    openBankingUtils:     OpenBankingUtils,
    requestSupport:       RequestSupport
)(implicit executionContext: ExecutionContext) {

  import requestSupport._

  def startOpenBankingJourney: Action[AnyContent] = actions.journeyAction.async { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val createSessionDataRequest: CreateSessionDataRequest =
      openBankingUtils
        .originDataFromJsd(journeyRequest.journey.journeySpecificData, None)
        .map(originSpecificSessionData => CreateSessionDataRequest(journeyRequest.journey.getAmountInPence, originSpecificSessionData, journeyRequest.journey.futureDatedPayment))
        .getOrElse(throw new RuntimeException("Unable to build createSessionDataRequest, so cannot start an OB journey"))

    openBankingConnector.startOpenBankingJourney(createSessionDataRequest)
      .map(createSessionDatRequest => Redirect(createSessionDatRequest.nextUrl))
  }

}
