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

import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.forms.EmailAddressForm
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.views.html.EmailAddressPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

import javax.inject.{Inject, Singleton}

@Singleton
class EmailAddressController @Inject() (
    actions:          Actions,
    requestSupport:   RequestSupport,
    emailAddressPage: EmailAddressPage,
    mcc:              MessagesControllerComponents
) extends FrontendController(mcc) {

  import requestSupport._

  val renderPage: Action[AnyContent] = actions.routedJourneyAction { implicit request: JourneyRequest[AnyContent] =>
    val form = request.readFromSession[EmailAddress](request.journeyId, Keys.email)
      .fold(EmailAddressForm.form()) { email => EmailAddressForm.form().fill(email) }
    Ok(emailAddressPage(form))
  }

  val submit: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    EmailAddressForm.form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[EmailAddress]) => BadRequest(emailAddressPage(form = formWithErrors)),
        { email =>
          Redirect(routes.AddressController.renderPage)
            .placeInSession(journeyRequest.journeyId, Keys.email -> email)

        }
      )
  }
}
