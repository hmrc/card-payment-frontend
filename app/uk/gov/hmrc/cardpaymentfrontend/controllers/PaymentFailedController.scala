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

import play.api.data.Form
import play.api.i18n.Messages.implicitMessagesProviderToMessages
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.forms.ChooseAPaymentMethodForm
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.OpenBanking
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.views.html.PaymentFailedPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class PaymentFailedController @Inject() (
    actions:             Actions,
    mcc:               MessagesControllerComponents,
    paymentFailedPage: PaymentFailedPage
) extends FrontendController(mcc) {

  val renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    Ok(paymentFailedPage(
      taxType = journeyRequest.journey.origin.lift.taxNameMessageKey,
      hasOpenBanking = journeyRequest.journey.origin.lift.paymentMethods().contains(OpenBanking)
    ))

  }

  val submit: Action[AnyContent] = Action { implicit request =>
    ChooseAPaymentMethodForm.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ChooseAPaymentMethodForm]) => BadRequest(paymentFailedPage(taxType = "Self Assessment", true, formWithErrors)),
        { validForm: ChooseAPaymentMethodForm =>
          validForm.chosenMethod match {
            case Some("open-banking") => Ok("we need to go to OB here")
            case Some("try-again")    => Redirect(uk.gov.hmrc.cardpaymentfrontend.controllers.routes.EmailAddressController.renderPage)
            case Some(_)              => throw new RuntimeException("This should never happen, form should prevent this from occurring")
            case None                 => throw new RuntimeException("This should never happen, form should prevent this from occurring")
          }

        }
      )
  }

}
