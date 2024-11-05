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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.Link
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.utils.{OriginExtraInfo, PaymentMethod, PaymentMethods}
import uk.gov.hmrc.cardpaymentfrontend.views.html.FeesPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class FeesController @Inject() (
    actions:         Actions,
    appConfig:       AppConfig,
    feesPage:        FeesPage,
    mcc:             MessagesControllerComponents,
    originExtraInfo: OriginExtraInfo,
    requestSupport:  RequestSupport
) extends FrontendController(mcc) {

  private[controllers] def twoDirectDebitsPrimaryLink(origin: Origin): Option[Link] = {
    origin match {
      case Origins.BtaEpayeBill => Some(Link(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-primary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case Origins.PfMgd => Some(Link(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-primary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case _ => None
    }
  }

  private[controllers] def twoDirectDebitsSecondaryLink(origin: Origin): Option[Link] = {
    origin match {
      case Origins.BtaEpayeBill => Some(Link(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-secondary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case Origins.PfMgd => Some(Link(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-secondary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case _ => None
    }
  }
  private[controllers] def altPaymentLinks(origin: Origin): Seq[Link] = {
    val bacsLink = Seq(
      Link(
        href       = Call("GET", "http://SomeURL"),
        linkId     = "bank-account-link-primary",
        messageKey = "card-fees.para2.bank-account"
      )
    )

    val openBankingLink = if (originExtraInfo.openBankingAllowed(origin)) {
      Seq(Link(
        href       = Call("GET", "http://SomeURLOpenBankingUrl"),
        linkId     = "open-banking-link",
        messageKey = "card-fees.para2.open-banking"
      ))
    } else Seq.empty[Link]

    val oneKindOfDDCondition: Boolean =
      (originExtraInfo.variableDirectDebitAllowed(origin) && !originExtraInfo.oneOffDirectDebitAllowed(origin)) ||
        (!originExtraInfo.variableDirectDebitAllowed(origin) && originExtraInfo.oneOffDirectDebitAllowed(origin))

    val oneKindOfDDlink = if (oneKindOfDDCondition) {
      Seq(Link(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link",
        messageKey = "card-fees.para2.direct-debit"
      ))
    } else Seq.empty[Link]

    val twoKindsOfDDPrimary = twoDirectDebitsPrimaryLink(origin) match {
      case Some(link) => Seq(link)
      case None       => Seq.empty[Link]
    }

    val twoKindsOfDDSecondary = twoDirectDebitsSecondaryLink(origin) match {
      case Some(link) => Seq(link)
      case None       => Seq.empty[Link]
    }

    bacsLink ++ openBankingLink ++ oneKindOfDDlink ++ twoKindsOfDDSecondary ++ twoKindsOfDDPrimary
  }

  //Note that when the Origin system is available this will be replaced with something more sensible
  def renderPage(origin: Origin): Action[AnyContent] = Action { implicit request =>
    val altPayments = altPaymentLinks(origin)
    if (altPayments.isEmpty) Redirect("http://nextpage.html")
    else Ok(feesPage(altPayments))
  }

  def renderPage0(): Action[AnyContent] = renderPage(Origins.AppSimpleAssessment)

  //Show open banking link
  def renderPage1(): Action[AnyContent] = renderPage(Origins.PfChildBenefitRepayments)

  //Show Variable Direct Debit link
  def renderPage2(): Action[AnyContent] = renderPage(Origins.PfSdil)

  //Show one off direct debit link
  def renderPage3(): Action[AnyContent] = renderPage(Origins.PfTpes)

  //Two kinds of DD with a primary link
  def renderPage4(): Action[AnyContent] = renderPage(Origins.BtaEpayeBill)

  // Two kinds of DD with secondary link
  def renderPage5(): Action[AnyContent] = renderPage(Origins.PfMgd)

  import requestSupport._

  def renderPageNew(): Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val altPayments = linksAvailableOnFeesPage(journeyRequest.journey.origin)
    if (altPayments.isEmpty) Redirect("http://nextpage.html")
    else Ok(feesPage(altPayments))
  }

  def submit: Action[AnyContent] = actions.journeyAction { _ =>
    Redirect(routes.EmailAddressController.renderPage)
  }

  private[controllers] def paymentMethodToBeShown(paymentMethod: PaymentMethod, paymentMethods: Set[PaymentMethod]): Boolean = paymentMethods.contains(paymentMethod)

  private[controllers] def linksAvailableOnFeesPage(origin: Origin): Seq[Link] = {

    val extendedOrigin = originExtraInfo.lift(origin)
    val paymentMethodsToShow: Set[PaymentMethod] = extendedOrigin.cardFeesPagePaymentMethods
    val showOpenBankingLink: Boolean = paymentMethodToBeShown(PaymentMethods.OpenBanking, paymentMethodsToShow)
    val showBankTransferLink: Boolean = paymentMethodToBeShown(PaymentMethods.Bacs, paymentMethodsToShow)
    val showOneOffDirectDebitLink: Boolean = paymentMethodToBeShown(PaymentMethods.OneOffDirectDebit, paymentMethodsToShow)

    val maybeOpenBankingLink = if (showOpenBankingLink) {
      Seq(Link(
        href       = Call("GET", routes.OpenBankingController.startOpenBankingJourney.url),
        linkId     = "open-banking-link",
        messageKey = "card-fees.para2.open-banking"
      ))
    } else Seq.empty[Link]

    val maybeBankTransferLink = if (showBankTransferLink) {
      Seq(Link(
        href       = Call("GET", appConfig.payFrontendBaseUrl + appConfig.bankTransferRelativeUrl),
        linkId     = "bank-transfer-link",
        messageKey = "card-fees.para2.bank-transfer"
      ))
    } else Seq.empty[Link]

    val maybeOneOffDirectDebitLink = if (showOneOffDirectDebitLink) {
      Seq(Link(
        href       = Call("GET", appConfig.payFrontendBaseUrl + appConfig.oneOffDirectDebitRelativeUrl),
        linkId     = "one-off-direct-debit-link",
        messageKey = "card-fees.para2.one-off-direct-debit"
      ))
    } else Seq.empty[Link]

    maybeOpenBankingLink ++ maybeBankTransferLink ++ maybeOneOffDirectDebitLink
  }

}
