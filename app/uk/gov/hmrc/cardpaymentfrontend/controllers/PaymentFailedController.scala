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

import payapi.corcommon.model.{Origin, Origins}
import play.api.data.Form
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.forms.ChooseAPaymentMethodForm
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.utils.{OpenBanking, OriginExtraInfo, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.views.html.PaymentFailedPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class PaymentFailedController @Inject() (
    mcc:               MessagesControllerComponents,
    paymentFailedPage: PaymentFailedPage,
    originExtraInfo:   OriginExtraInfo
) extends FrontendController(mcc) {

  def renderPage(origin: Origin): Action[AnyContent] = Action { implicit request =>
    val liftedOrigin: ExtendedOrigin = originExtraInfo.lift(origin)
    val paymentMethods: Set[PaymentMethod] = liftedOrigin.paymentMethods()
    Ok(paymentFailedPage(origin.toTaxType.toString, paymentMethods.contains(OpenBanking()), ChooseAPaymentMethodForm.form))
  }

  def renderPage0(): Action[AnyContent] = renderPage(Origins.PfP800)

  def renderPage1(): Action[AnyContent] = renderPage(Origins.PfSa)

  def renderPage2(): Action[AnyContent] = renderPage(Origins.BcPngr)

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
