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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import play.api.Logging
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.forms.AddressForm
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.{CountriesService, PaymentService}
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps
import uk.gov.hmrc.cardpaymentfrontend.views.html.AddressPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AddressController @Inject() (
    actions:          Actions,
    addressPage:      AddressPage,
    countriesService: CountriesService,
    mcc:              MessagesControllerComponents,
    requestSupport:   RequestSupport,
    paymentService:   PaymentService
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) with Logging {

  import requestSupport._

  val renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val form: Form[Address] = addressInSession.fold(AddressForm.form())(address => AddressForm.form().fill(address))
    logger.info(s"address in session: ${addressInSession.toString}")
    Ok(addressPage(form, countriesService.getCountries))
  }

  val submit: Action[AnyContent] = actions.journeyAction.async { implicit journeyRequest: JourneyRequest[AnyContent] =>
    AddressForm.form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Address]) => Future.successful(BadRequest(addressPage(form = formWithErrors, countriesService.getCountries))),
        { address =>

          val successResult = Redirect(routes.CheckYourAnswersController.renderPage).placeInSession[Address](journeyRequest.journeyId, Keys.address -> address)

          // If an address is not present in session, proceed. If there is one in session, check if it's changed. If it has changed, reset order.
          addressInSession.fold(Future.successful(successResult)) { addressFromSession =>
            if (addressIsDifferent(address, addressFromSession)) {
              logger.info("Address being submitted differs from what is currently in journey, resetting order.")
              for {
                _ <- paymentService.resetSentJourney()
              } yield successResult
            } else Future.successful(successResult)
          }
        }
      )
  }

  private[controllers] def addressInSession(implicit journeyRequest: JourneyRequest[_]): Option[Address] =
    journeyRequest.readFromSession[Address](journeyRequest.journeyId, Keys.address)

  private[controllers] def addressIsDifferent(addressA: Address, addressB: Address): Boolean = addressA =!= addressB
}
