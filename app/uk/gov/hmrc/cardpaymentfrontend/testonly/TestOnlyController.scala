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

package uk.gov.hmrc.cardpaymentfrontend.testonly

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.connectors.PayApiConnector
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

@Singleton
class TestOnlyController @Inject() (
    actions:         Actions,
    mcc:             MessagesControllerComponents,
    payApiConnector: PayApiConnector
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) {

  val showJourney: Action[AnyContent] = Action.async { implicit request =>
    payApiConnector.findLatestJourneyBySessionId()
      .map(maybeJourney => Ok(Json.toJson(maybeJourney)))
  }

  val addAnEmailToSession: Action[AnyContent] = actions.journeyAction { implicit request: JourneyRequest[AnyContent] =>
    Ok("email added to session").placeInSession[EmailAddress](request.journeyId, (Keys.email, EmailAddress("testemail@blah.com")))
  }

  val removeAddress: Action[AnyContent] = actions.journeyAction { implicit request =>
    Ok("removed address from session").removingJourneyFromSession(request.journeyId, Keys.address)
  }

}
