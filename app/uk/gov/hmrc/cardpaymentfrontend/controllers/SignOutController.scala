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

import com.google.inject.{Inject, Singleton}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.Actions
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.views.html.TimedOutPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

@Singleton
class SignOutController @Inject() (
    actions:      Actions,
    mcc:          MessagesControllerComponents,
    appConfig:    AppConfig,
    timedOutPage: TimedOutPage
) extends FrontendController(mcc) with I18nSupport {

  private lazy val signOutFromTimeoutRedirect = {
    val continueUrl = s"${appConfig.cardPaymentFrontendBaseUrl}${routes.SignOutController.timedOut.url}"
    Redirect(s"${appConfig.signOutUrl}?continue=$continueUrl")
  }

  val signOutFromTimeout: Action[AnyContent] = actions.default { _ =>
    signOutFromTimeoutRedirect
  }

  val signOut: Action[AnyContent] = actions.default { _ =>
    val continueUrl: String = "%2Ffeedback%2Fpay-online"
    Redirect(s"${appConfig.signOutUrl}?continue=$continueUrl")
  }

  val timedOut: Action[AnyContent] = actions.default { implicit request =>
    Ok(timedOutPage()).withNewSession
  }

  val keepAlive: Action[AnyContent] = actions.default { _ =>
    Ok("Okay")
  }

}
