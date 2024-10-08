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
import uk.gov.hmrc.cardpaymentfrontend.models.AltPaymentLink
import uk.gov.hmrc.cardpaymentfrontend.utils.OriginExtraInfo
import uk.gov.hmrc.cardpaymentfrontend.views.html.FeesPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class FeesController @Inject() (
    originExtraInfo: OriginExtraInfo,
    mcc:             MessagesControllerComponents,
    feesPage:        FeesPage
) extends FrontendController(mcc) {
  private[controllers] def twoDirectDebitsPrimaryLink(origin: Origin): Option[AltPaymentLink] = {
    origin match {
      case Origins.BtaEpayeBill => Some(AltPaymentLink(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-primary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case Origins.PfMgd => Some(AltPaymentLink(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-primary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case _ => None
    }
  }

  private[controllers] def twoDirectDebitsSecondaryLink(origin: Origin): Option[AltPaymentLink] = {
    origin match {
      case Origins.BtaEpayeBill => Some(AltPaymentLink(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-secondary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case Origins.PfMgd => Some(AltPaymentLink(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link-both-secondary",
        messageKey = "card-fees.para2.direct-debit"
      ))
      case _ => None
    }
  }
  private[controllers] def altPaymentLinks(origin: Origin): Seq[AltPaymentLink] = {
    val bacsLink = Seq(
      AltPaymentLink(
        href       = Call("GET", "http://SomeURL"),
        linkId     = "bank-account-link-primary",
        messageKey = "card-fees.para2.bank-account"
      )
    )

    val openBankingLink = if (originExtraInfo.openBankingAllowed(origin)) {
      Seq(AltPaymentLink(
        href       = Call("GET", "http://SomeURLOpenBankingUrl"),
        linkId     = "open-banking-link",
        messageKey = "card-fees.para2.open-banking"
      ))
    } else Seq.empty[AltPaymentLink]

    val oneKindOfDDCondition: Boolean =
      (originExtraInfo.variableDirectDebitAllowed(origin) && !originExtraInfo.oneOffDirectDebitAllowed(origin)) ||
        (!originExtraInfo.variableDirectDebitAllowed(origin) && originExtraInfo.oneOffDirectDebitAllowed(origin))

    val oneKindOfDDlink = if (oneKindOfDDCondition) {
      Seq(AltPaymentLink(
        href       = Call("GET", "http://SomeDDUrl"),
        linkId     = "direct-debit-link",
        messageKey = "card-fees.para2.direct-debit"
      ))
    } else Seq.empty[AltPaymentLink]

    val twoKindsOfDDPrimary = twoDirectDebitsPrimaryLink(origin) match {
      case Some(link) => Seq(link)
      case None       => Seq.empty[AltPaymentLink]
    }

    val twoKindsOfDDSecondary = twoDirectDebitsSecondaryLink(origin) match {
      case Some(link) => Seq(link)
      case None       => Seq.empty[AltPaymentLink]
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

}
