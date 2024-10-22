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
import play.api.i18n._
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow.summarise
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.utils.OriginExtraInfo
import uk.gov.hmrc.cardpaymentfrontend.views.html.CheckYourAnswersPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryList

import javax.inject.{Inject, Singleton}

@Singleton()
class CheckYourAnswersController @Inject() (
                                           actions: Actions,
    mcc:                  MessagesControllerComponents,
    originExtraInfo:      OriginExtraInfo,
    checkYourAnswersPage: CheckYourAnswersPage,
                                           requestSupport: RequestSupport
) extends FrontendController(mcc) {

  import requestSupport._

  def renderPage(origin: Origin): Action[AnyContent] = actions.journeyAction { implicit request: JourneyRequest[AnyContent] =>
    val liftedOrigin: ExtendedOrigin = originExtraInfo.lift(origin)
    val summaryList = liftedOrigin.checkYourAnswersRows(request).map(summarise)
    Ok(checkYourAnswersPage(liftedOrigin.reference(request), SummaryList(summaryList)))
  }

  def renderPage0(): Action[AnyContent] = renderPage(Origins.PfSa)

  def renderPage1(): Action[AnyContent] = renderPage(Origins.PfVat)
}
