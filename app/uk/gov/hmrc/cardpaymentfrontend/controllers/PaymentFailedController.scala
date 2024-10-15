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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.forms.ChooseAPaymentMethodForm
import uk.gov.hmrc.cardpaymentfrontend.views.html.{PaymentFailedObAvailablePage, PaymentFailedPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class PaymentFailedController @Inject() (
    mcc:                          MessagesControllerComponents,
    paymentFailedPage:            PaymentFailedPage,
    paymentFailedObAvailablePage: PaymentFailedObAvailablePage
) extends FrontendController(mcc) {

  val renderPage: Action[AnyContent] = Action { implicit request =>
    Ok(paymentFailedPage(taxType = "Self Assessment"))
  }

  val renderPageObAvailable: Action[AnyContent] = Action { implicit request =>
    Ok(paymentFailedObAvailablePage(ChooseAPaymentMethodForm.form))
  }

  val submit: Action[AnyContent] = Action { implicit request =>
    ChooseAPaymentMethodForm.form
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[ChooseAPaymentMethodForm]) => BadRequest(paymentFailedObAvailablePage(formWithErrors)),
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
