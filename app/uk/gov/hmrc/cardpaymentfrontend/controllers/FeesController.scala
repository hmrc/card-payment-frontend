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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdBtaSdil, JsdPfSdil}
import payapi.corcommon.model.taxes.other.SoftDrinksIndustryLevyRef
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.actions.{Actions, JourneyRequest}
import uk.gov.hmrc.cardpaymentfrontend.config.AppConfig
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedOrigin.OriginExtended
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.ExtendedPfVat
import uk.gov.hmrc.cardpaymentfrontend.models.{Link, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.views.html.FeesPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}

@Singleton
class FeesController @Inject() (
    actions:        Actions,
    appConfig:      AppConfig,
    feesPage:       FeesPage,
    mcc:            MessagesControllerComponents,
    requestSupport: RequestSupport
) extends FrontendController(mcc) {

  import requestSupport._

  def renderPage: Action[AnyContent] = actions.journeyAction { implicit journeyRequest: JourneyRequest[AnyContent] =>
    val altPayments = linksAvailableOnFeesPage(journeyRequest.journey.journeySpecificData)
    Ok(feesPage(altPayments))
  }

  def submit: Action[AnyContent] = actions.journeyAction { _ =>
    Redirect(routes.EmailAddressController.renderPage)
  }

  private[controllers] def paymentMethodToBeShown(paymentMethod: PaymentMethod, paymentMethods: Set[PaymentMethod]): Boolean = paymentMethods.contains(paymentMethod)

  private[controllers] def linksAvailableOnFeesPage(jsd: JourneySpecificData): Seq[Link] = {
    val extendedOrigin = jsd.origin.lift
    val paymentMethodsToShow: Set[PaymentMethod] = extendedOrigin.cardFeesPagePaymentMethods
    val showOpenBankingLink: Boolean = paymentMethodToBeShown(PaymentMethod.OpenBanking, paymentMethodsToShow)
    val showBankTransferLink: Boolean = paymentMethodToBeShown(PaymentMethod.Bacs, paymentMethodsToShow)
    val showOneOffDirectDebitLink: Boolean = paymentMethodToBeShown(PaymentMethod.OneOffDirectDebit, paymentMethodsToShow)
    val showVariableDirectDebitLink: Boolean = paymentMethodToBeShown(PaymentMethod.VariableDirectDebit, paymentMethodsToShow)
    val showDirectDebitLink: Boolean = paymentMethodToBeShown(PaymentMethod.DirectDebit, paymentMethodsToShow) && journeyShouldShowMdtpDirectDebit(jsd)

      def pfVatChargeReferenceExists: Boolean = extendedOrigin match {
        case ExtendedPfVat => ExtendedPfVat.chargeReference.apply(jsd).isDefined
        case _             => false
      }

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

    val maybeVariableDirectDebitLink = if (showVariableDirectDebitLink && !pfVatChargeReferenceExists) {
      Seq(Link(
        href       = Call("GET", appConfig.payFrontendBaseUrl + appConfig.variableDirectDebitRelativeUrl),
        linkId     = "variable-direct-debit-link",
        messageKey = "card-fees.para2.variable-direct-debit"
      ))
    } else Seq.empty[Link]

    val maybeOneOffDirectDebitLink = if (showOneOffDirectDebitLink) {
      Seq(Link(
        href       = Call("GET", appConfig.payFrontendBaseUrl + appConfig.oneOffDirectDebitRelativeUrl),
        linkId     = "one-off-direct-debit-link",
        messageKey = "card-fees.para2.one-off-direct-debit"
      ))
    } else Seq.empty[Link]

    //the one MDTP owned by oceans 11
    val maybeDirectDebitLink = if (showDirectDebitLink) {
      Seq(Link(
        href       = Call("GET", appConfig.payFrontendBaseUrl + appConfig.directDebitRelativeUrl),
        linkId     = "direct-debit-link",
        messageKey = "card-fees.para2.direct-debit"
      ))
    } else Seq.empty[Link]

    maybeOpenBankingLink ++ maybeBankTransferLink ++ maybeVariableDirectDebitLink ++ maybeOneOffDirectDebitLink ++ maybeDirectDebitLink
  }

  //only required for Sdil currently, because the journey contains two sub types of ref (why?!) we need to check for the subtype of ref, as only one supports dd...
  private[controllers] def journeyShouldShowMdtpDirectDebit: JourneySpecificData => Boolean = {
    case j: JsdPfSdil => j.softDrinksIndustryLevyRef.exists {
      case r if r.value matches SoftDrinksIndustryLevyRef.softDrinksIndustryLevyRefRegex => true
      case _ => false
    }
    case _: JsdBtaSdil => true
    case _             => false
  }

}
