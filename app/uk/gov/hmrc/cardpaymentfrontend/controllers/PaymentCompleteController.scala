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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.cardpaymentfrontend.views.html.PaymentCompletePage

import javax.inject.Inject

class PaymentCompleteController @Inject() (
    mcc:                 MessagesControllerComponents,
    paymentCompletePage: PaymentCompletePage
) extends FrontendController(mcc) {

  val renderPage: Action[AnyContent] = Action { implicit request =>
    Ok(paymentCompletePage(
      // all hard coded for now...
      taxReference      = "1234567895K",
      taxType           = "Self assessment",
      amountInPounds    = "£12.34",
      paymentDate       = "7 October 2024",
      maybeEmailAddress = Some(EmailAddress("blah@blah.com"))
    ))
  }
}
