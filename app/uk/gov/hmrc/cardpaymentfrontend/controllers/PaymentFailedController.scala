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
    Ok(paymentFailedPage())
  }

  val submit: Action[AnyContent] = Action { implicit request =>
    Ok(paymentFailedObAvailablePage())
  }

  //  private def selectViewForJourney(maybeTraceId: Option[TraceId], form: Form[ChooseAPaymentMethod], journey: Journey[JourneySpecificData], surveyUrl: Url)(implicit request: JourneyRequest[_]): Result = {
  //    if (PaymentMethods.openBankingAllowed(journey.origin)) {
  //      Ok(paymentFailedObAvailablePage(
  //        maybeTraceId,
  //        form,
  //        journey._id,
  //        journey.contentOptions.title,
  //        journey.contentOptions.isWelshSupported,
  //        surveyUrl,
  //        journey.taxType
  //      ))
  //    } else
  //      Ok(paymentFailedPage(
  //        journey._id,
  //        journey.contentOptions.title,
  //        journey.contentOptions.isWelshSupported,
  //        surveyUrl,
  //        journey.taxType
  //      ))
  //  }
  //
  //  def submitChooseAPaymentMethod(maybeTraceId: Option[TraceId]): Action[AnyContent] = actions.journeyAction.async { implicit request =>
  //    val journey = request.journey
  //
  //    Forms.chooseCardFailedOngoingPaymentMethod.bindFromRequest().fold(
  //      formWithErrors => selectViewForJourney(maybeTraceId, formWithErrors, journey, Url(controllers.routes.SurveyController.startSurvey().url)),
  //      {
  //        case ChooseAPaymentMethod(Some(option)) =>
  //          option match {
  //            case "open-banking" => Redirect(controllers.routes.OpenBankingController.payByOpenBanking(maybeTraceId).url)
  //            case "another-way"  => Redirect(controllers.routes.ChooseAWayToPayController.chooseAWayToPay(maybeTraceId).url)
  //            case "try-again"    => Redirect(controllers.routes.EmailController.show(maybeTraceId).url)
  //          }
  //
  //        case ChooseAPaymentMethod(_) =>
  //          throw new RuntimeException(s"Unsupported chosenPaymentType ")
  //      }
  //    )
  //  }

}
