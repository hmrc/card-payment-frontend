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

import payapi.corcommon.model.PaymentStatuses
import play.api.Logging
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.forms.EmailAddressForm
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.PaymentService
import uk.gov.hmrc.cardpaymentfrontend.views.html.EmailAddressPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailAddressController @Inject() (
  actions:          Actions,
  requestSupport:   RequestSupport,
  emailAddressPage: EmailAddressPage,
  mcc:              MessagesControllerComponents,
  paymentService:   PaymentService
)(implicit executionContext: ExecutionContext)
    extends FrontendController(mcc)
    with Logging {

  import requestSupport._

  val renderPage: Action[AnyContent] = actions.routedJourneyAction { implicit request: JourneyRequest[AnyContent] =>
    val form = emailInSession.fold(EmailAddressForm.form()) { email => EmailAddressForm.form().fill(email) }
    Ok(emailAddressPage(form))
  }

  val renderPageAfterReset: Action[AnyContent] = actions.routedJourneyAction.async { implicit request: JourneyRequest[AnyContent] =>
    request.journey.status match {
      case PaymentStatuses.Created | PaymentStatuses.Successful | PaymentStatuses.SoftDecline | PaymentStatuses.Validated =>
        Future.successful(Redirect(routes.EmailAddressController.renderPage))
      case PaymentStatuses.Sent                                                                                           =>
        logger.info("User journey in Sent state, attempting to reset order and status.")
        paymentService.resetSentJourneyThenResult(Redirect(routes.EmailAddressController.renderPage))
      case PaymentStatuses.Failed | PaymentStatuses.Cancelled                                                             =>
        paymentService.createCopyOfCancelledOrFailedJourney().map(_ => Redirect(routes.EmailAddressController.renderPage))
    }
  }

  val submit: Action[AnyContent] = actions.journeyAction.async { implicit journeyRequest: JourneyRequest[AnyContent] =>
    EmailAddressForm
      .form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[EmailAddress]) => Future.successful(BadRequest(emailAddressPage(form = formWithErrors))),
        { email =>

          val successResult = Redirect(routes.AddressController.renderPage).placeInSession(journeyRequest.journeyId, Keys.email -> email)

          // If an email is not present in session, proceed. If there is one in session, check if it's changed. If it has changed, reset order.
          emailInSession.fold(Future.successful(successResult)) { emailFromSession =>
            if (emailIsDifferent(email, emailFromSession)) {
              logger.info("Email being submitted differs from what is currently in journey, resetting order.")
              for {
                _ <- paymentService.resetSentJourney()
              } yield successResult
            } else Future.successful(successResult)
          }
        }
      )
  }

  private[controllers] def emailInSession(implicit journeyRequest: JourneyRequest[_]): Option[EmailAddress] =
    journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email)

  private[controllers] def emailIsDifferent(emailA: EmailAddress, emailB: EmailAddress): Boolean = emailA =!= emailB

}
