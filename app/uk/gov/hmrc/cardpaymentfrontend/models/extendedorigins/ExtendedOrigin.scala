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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.corcommon.model.{Origin, Origins, Reference}
import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import play.api.i18n.Messages
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.session.JourneySessionSupport._

import java.time.LocalDate

trait ExtendedOrigin {

  def serviceNameMessageKey: String
  def taxNameMessageKey: String

  def amount(request: JourneyRequest[AnyContent]): String = s"£${request.journey.getAmountInPence.inPoundsRoundedFormatted}"

  def reference(request: JourneyRequest[AnyContent]): String = {
    request.journey.journeySpecificData.reference match {
      case Some(Reference(ref)) => ref
      case None                 => "???" //todo:... and log
    }
  }

  //denotes which links/payment methods to show on the card-fees page.
  def cardFeesPagePaymentMethods: Set[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod]

  def checkYourAnswersPaymentDateRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    if (showFuturePayment(journeyRequest)) {
      Some(CheckYourAnswersRow(
        titleMessageKey = "check-your-details.payment-date",
        value           = Seq("check-your-details.payment-date.today"),
        changeLink      = Some(Link(
          href       = Call("GET", "some-link-to-pay-frontend"),
          linkId     = "check-your-details-payment-date-change-link",
          messageKey = "check-your-details.change"
        ))
      ))
    } else {
      None
    }

  }

  // If dueDate is not today, we do not show the payment date row as FDP is not supported by card payments
  def showFuturePayment(journeyRequest: JourneyRequest[AnyContent]): Boolean = {
    journeyRequest.journey.journeySpecificData.dueDate.fold(false)(LocalDate.now().isBefore)
  }

  def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow]

  def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = None

  def checkYourAnswersAmountSummaryRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = Some(CheckYourAnswersRow(
    titleMessageKey = "check-your-details.total-to-pay",
    value           = Seq(amount(journeyRequest)),
    changeLink      = Some(Link(
      href       = Call("GET", "some-link-to-pay-frontend"),
      linkId     = "check-your-details-amount-change-link",
      messageKey = "check-your-details.change"
    ))
  ))

  def checkYourAnswersEmailAddressRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    val maybeEmail: Option[EmailAddress] = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email)
    maybeEmail.map { email =>
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.email-address",
        value           = Seq(email.value),
        changeLink      = Some(Link(
          href       = Call("GET", "some-link-to-address-page-on-card-payment-frontend"),
          linkId     = "check-your-details-email-address-change-link",
          messageKey = "check-your-details.change"
        ))
      )
    }
  }

  // TODO: Update tests to not include country - check doesn't show country
  def checkYourAnswersCardBillingAddressRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    //todo error? we can't take a card payment without an address
    val addressFromSession: Address = journeyRequest.readFromSession[Address](journeyRequest.journeyId, Keys.address).getOrElse(throw new RuntimeException("Cannot take a card payment without an address"))
    val addressValues: Seq[String] = Seq[String](
      addressFromSession.line1,
      addressFromSession.line2.getOrElse(""),
      addressFromSession.city.getOrElse(""),
      addressFromSession.county.getOrElse(""),
      addressFromSession.postcode
    ).filter(_.nonEmpty)

    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.card-billing-address",
      value           = addressValues,
      changeLink      = Some(Link(
        href       = Call("GET", "some-link-to-address-page-on-card-payment-frontend"),
        linkId     = "check-your-details-card-billing-address-change-link",
        messageKey = "check-your-details.change"
      ))
    ))
  }

  //todo rename, it's not quite right -- or delete when not used anymore.
  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow]
  def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData]

  //email related content
  def emailTaxTypeMessageKey: String

  //payments survey stuff
  def surveyAuditName: String
  def surveyReturnHref: String
  def surveyReturnMessageKey: String
  def surveyIsWelshSupported: Boolean
  def surveyBannerTitle: String

}

object ExtendedOrigin {
  implicit class OriginExtended(origin: Origin) {
    def lift: ExtendedOrigin = origin match {
      case Origins.PfSa                     => ExtendedPfSa
      case Origins.PfVat                    => ExtendedPfVat
      case Origins.PfCt                     => new DefaultExtendedOrigin
      case Origins.PfEpayeNi                => new DefaultExtendedOrigin
      case Origins.PfEpayeLpp               => new DefaultExtendedOrigin
      case Origins.PfEpayeSeta              => new DefaultExtendedOrigin
      case Origins.PfEpayeLateCis           => new DefaultExtendedOrigin
      case Origins.PfEpayeP11d              => new DefaultExtendedOrigin
      case Origins.PfSdlt                   => new DefaultExtendedOrigin
      case Origins.PfCds                    => new DefaultExtendedOrigin
      case Origins.PfOther                  => new DefaultExtendedOrigin
      case Origins.PfP800                   => new ExtendedPfP800
      case Origins.PtaP800                  => new DefaultExtendedOrigin
      case Origins.PfClass2Ni               => new DefaultExtendedOrigin
      case Origins.PfInsurancePremium       => new DefaultExtendedOrigin
      case Origins.PfPsAdmin                => new DefaultExtendedOrigin
      case Origins.BtaSa                    => ExtendedBtaSa
      case Origins.AppSa                    => new DefaultExtendedOrigin
      case Origins.BtaVat                   => new DefaultExtendedOrigin
      case Origins.BtaEpayeBill             => new DefaultExtendedOrigin
      case Origins.BtaEpayePenalty          => new DefaultExtendedOrigin
      case Origins.BtaEpayeInterest         => new DefaultExtendedOrigin
      case Origins.BtaEpayeGeneral          => new DefaultExtendedOrigin
      case Origins.BtaClass1aNi             => new DefaultExtendedOrigin
      case Origins.BtaCt                    => new DefaultExtendedOrigin
      case Origins.BtaSdil                  => new DefaultExtendedOrigin
      case Origins.BcPngr                   => new DefaultExtendedOrigin
      case Origins.Parcels                  => new DefaultExtendedOrigin
      case Origins.DdVat                    => new DefaultExtendedOrigin
      case Origins.DdSdil                   => new DefaultExtendedOrigin
      case Origins.VcVatReturn              => new DefaultExtendedOrigin
      case Origins.VcVatOther               => new DefaultExtendedOrigin
      case Origins.ItSa                     => ExtendedItSa
      case Origins.Amls                     => new DefaultExtendedOrigin
      case Origins.Ppt                      => new DefaultExtendedOrigin
      case Origins.PfCdsCash                => new DefaultExtendedOrigin
      case Origins.PfPpt                    => new DefaultExtendedOrigin
      case Origins.PfSpiritDrinks           => new DefaultExtendedOrigin
      case Origins.PfInheritanceTax         => new DefaultExtendedOrigin
      case Origins.Mib                      => new DefaultExtendedOrigin
      case Origins.PfClass3Ni               => new DefaultExtendedOrigin
      case Origins.PtaSa                    => ExtendedPtaSa
      case Origins.PfWineAndCider           => new DefaultExtendedOrigin
      case Origins.PfBioFuels               => new DefaultExtendedOrigin
      case Origins.PfAirPass                => new DefaultExtendedOrigin
      case Origins.PfMgd                    => new DefaultExtendedOrigin
      case Origins.PfBeerDuty               => new DefaultExtendedOrigin
      case Origins.PfGamingOrBingoDuty      => new DefaultExtendedOrigin
      case Origins.PfGbPbRgDuty             => new DefaultExtendedOrigin
      case Origins.PfLandfillTax            => new DefaultExtendedOrigin
      case Origins.PfSdil                   => new DefaultExtendedOrigin
      case Origins.PfAggregatesLevy         => new DefaultExtendedOrigin
      case Origins.PfClimateChangeLevy      => new DefaultExtendedOrigin
      case Origins.PfSimpleAssessment       => new DefaultExtendedOrigin
      case Origins.PtaSimpleAssessment      => new DefaultExtendedOrigin
      case Origins.AppSimpleAssessment      => new DefaultExtendedOrigin
      case Origins.PfTpes                   => new DefaultExtendedOrigin
      case Origins.CapitalGainsTax          => new DefaultExtendedOrigin
      case Origins.EconomicCrimeLevy        => new DefaultExtendedOrigin
      case Origins.PfEconomicCrimeLevy      => new DefaultExtendedOrigin
      case Origins.PfJobRetentionScheme     => new DefaultExtendedOrigin
      case Origins.JrsJobRetentionScheme    => new DefaultExtendedOrigin
      case Origins.PfImportedVehicles       => new DefaultExtendedOrigin
      case Origins.PfChildBenefitRepayments => new DefaultExtendedOrigin
      case Origins.NiEuVatOss               => new DefaultExtendedOrigin
      case Origins.PfNiEuVatOss             => new DefaultExtendedOrigin
      case Origins.NiEuVatIoss              => new DefaultExtendedOrigin
      case Origins.PfNiEuVatIoss            => new DefaultExtendedOrigin
      case Origins.PfAmls                   => new DefaultExtendedOrigin
      case Origins.PfAted                   => new DefaultExtendedOrigin
      case Origins.PfCdsDeferment           => new DefaultExtendedOrigin
      case Origins.PfTrust                  => new DefaultExtendedOrigin
      case Origins.PtaClass3Ni              => new DefaultExtendedOrigin
      case Origins.PfAlcoholDuty            => ExtendedPfAlcoholDuty
      case Origins.AlcoholDuty              => ExtendedAlcoholDuty
      case Origins.VatC2c                   => new DefaultExtendedOrigin
      case Origins.`3psSa`                  => new DefaultExtendedOrigin
    }
  }
}
