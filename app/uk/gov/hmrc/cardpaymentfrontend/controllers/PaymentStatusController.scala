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
import uk.gov.hmrc.cardpaymentfrontend.actions.Actions
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.views.html.iframe.{IframeContainerPage, RedirectToParentPage}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton()
class PaymentStatusController @Inject() (
    actions:          Actions,
    mcc:              MessagesControllerComponents,
    requestSupport:   RequestSupport,
    iframeContainer:  IframeContainerPage,
    redirectToParent: RedirectToParentPage
) extends FrontendController(mcc) {

  import requestSupport._

  def showIframe(iframeUrl: String): Action[AnyContent] = Action { implicit req =>
    Ok(iframeContainer(iframeUrl))
  }

  def returnToHmrc(transactionReference: String): Action[AnyContent] = actions.default { implicit request =>
    Ok(redirectToParent(transactionReference))
  }

  //todo append something to the return url so we can extract/work out the session/journey - are we allowed to do this or do we use session?
  def paymentStatus(transactionReference: String): Action[AnyContent] = actions.default.async { _ =>
    Future.successful(Ok(s"We're back from the iframe!\nNow we need to just do the check payment status bit and auth and settle. ${transactionReference}"))
  }

}
