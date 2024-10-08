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
import uk.gov.hmrc.cardpaymentfrontend.forms.AddressForm
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.services.CountriesService
import uk.gov.hmrc.cardpaymentfrontend.views.html.AddressPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class AddressController @Inject() (
    mcc:              MessagesControllerComponents,
    addressPage:      AddressPage,
    countriesService: CountriesService
) extends FrontendController(mcc) {

  val renderPage: Action[AnyContent] = Action { implicit request =>
    Ok(addressPage(AddressForm.form(), countriesService.getCountries))
  }

  val submit: Action[AnyContent] = Action { implicit request =>
    AddressForm.form()
      .bindFromRequest()
      .fold(
        (formWithErrors: Form[Address]) => BadRequest(addressPage(form = formWithErrors, countriesService.getCountries)),
        { _ =>
          Ok("Happy with the address entered")
        }
      )
  }

}
