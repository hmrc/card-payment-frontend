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

import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import uk.gov.hmrc.cardpaymentfrontend.actions.Actions
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.services.CardPaymentService
import uk.gov.hmrc.cardpaymentfrontend.views.html.iframe.{IframeContainerPage, RedirectToParentPage}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl.idFunctor
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrlPolicy.Id
import uk.gov.hmrc.play.bootstrap.binders.{AbsoluteWithHostnameFromAllowlist, RedirectUrl, RedirectUrlPolicy}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton()
class PaymentStatusController @Inject() (
    actions:            Actions,
    appConfig:          AppConfig,
    cardPaymentService: CardPaymentService,
    mcc:                MessagesControllerComponents,
    requestSupport:     RequestSupport,
    iframeContainer:    IframeContainerPage,
    redirectToParent:   RedirectToParentPage
)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) {

  import requestSupport._

  private val redirectUrlPolicy: RedirectUrlPolicy[Id] = AbsoluteWithHostnameFromAllowlist(appConfig.iframeHostNameAllowList)

  //todo need to write a test for this, where we override the allow list or something to trigger bad request.
  def showIframe(iframeUrl: RedirectUrl): Action[AnyContent] = Action { implicit req =>
    iframeUrl
      .getEither[Id](redirectUrlPolicy)
      .fold[Result](
        _ => BadRequest("Bad url provided that doesn't match the redirect policy. Check allow list if this is not expected."),
        safeUrl => Ok(iframeContainer(safeUrl.url))
      )
  }

  def returnToHmrc(transactionReference: String): Action[AnyContent] = actions.default { implicit request =>
    Ok(redirectToParent(transactionReference))
  }

  //todo append something to the return url so we can extract/work out the session/journey - are we allowed to do this or do we use session?
  def paymentStatus(traceId: String): Action[AnyContent] = actions.journeyAction.async { implicit journeyRequest =>
    implicit val hc: HeaderCarrier = requestSupport.hc
    val transactionRefFromJourney: Option[String] = journeyRequest.journey.order.map(_.transactionReference.value)
    for {
      paymentStatus <- cardPaymentService.checkPaymentStatus(transactionRefFromJourney.getOrElse(throw new RuntimeException("Could not find transaction ref, pay-api probably didn't update.")))
      authAndCaptureResult <- maybeAuthAndSettleResult(transactionRefFromJourney, paymentStatus.value)
    } yield Ok(
      s"""We're back from the iframe!\n""" +
        s"""traceId: $traceId\n""" +
        s"""we now need to update the journey in pay-api with the completed payment info\n""" +
        s"""CheckPaymentStatus response:\n${paymentStatus.value.toString}\n\n""" +
        s"""authAndSettle response:\n${authAndCaptureResult.body}\n\n"""
    )
  }

  //todo in future, lets check the result json and redirect accordingly. (i.e. if it's failed etc)
  private def maybeAuthAndSettleResult(transactionRefFromJourney: Option[String], shouldAuthAndSettle: Boolean)(implicit headerCarrier: HeaderCarrier): Future[HttpResponse] = {
    if (shouldAuthAndSettle) {
      cardPaymentService.authAndSettle(transactionRefFromJourney.getOrElse(throw new RuntimeException("Could not find transaction ref, therefore we can't auth and settle.")))
    } else {
      Future.successful(HttpResponse.apply(500, "Cannot auth and capture when status is not acceptable. We should redirect accordingly."))
    }
  }

}
