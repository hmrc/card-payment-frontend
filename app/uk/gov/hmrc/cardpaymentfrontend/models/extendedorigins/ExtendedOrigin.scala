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

import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import payapi.corcommon.model.{Origin, Origins, Reference}
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.models._
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
  // This denotes which payment methods are available for the given Origin/TaxRegime
  def paymentMethods(): Set[PaymentMethod]

  def checkYourAnswersPaymentDateRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    if (showFuturePayment(journeyRequest)) {
      Some(CheckYourAnswersRow(
        titleMessageKey = "check-your-details.payment-date",
        value           = Seq("check-your-details.payment-date.today"),
        changeLink      = Some(Link(
          href       = Call("GET", s"$payFrontendBaseUrl/change-when-do-you-want-to-pay?toPayFrontendConfirmation=true"),
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

  protected def changeReferenceUrl(payFrontendBaseUrl: String): String = s"$payFrontendBaseUrl/pay-by-card-change-reference-number"

  //hint: the checkYourAnswersReferenceRow should only include a change link when the journey is not prepopulated, i.e., user has manually entered their reference.
  def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow]

  def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = None

  def checkYourAnswersAmountSummaryRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = Some(CheckYourAnswersRow(
    titleMessageKey = "check-your-details.total-to-pay",
    value           = Seq(amount(journeyRequest)),
    changeLink      = Some(Link(
      href       = Call("GET", s"$payFrontendBaseUrl/change-amount?showSummary=false&stayOnPayFrontend=false"),
      linkId     = "check-your-details-amount-change-link",
      messageKey = "check-your-details.change"
    ))
  ))

  def checkYourAnswersEmailAddressRow(journeyRequest: JourneyRequest[AnyContent]): Option[CheckYourAnswersRow] = {
    val maybeEmail: Option[EmailAddress] = journeyRequest.readFromSession[EmailAddress](journeyRequest.journeyId, Keys.email)
    maybeEmail.filter(!_.value.isBlank)
      .map { email =>
        CheckYourAnswersRow(
          titleMessageKey = "check-your-details.email-address",
          value           = Seq(email.value),
          changeLink      = Some(Link(
            href       = uk.gov.hmrc.cardpaymentfrontend.controllers.routes.EmailAddressController.renderPage,
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
        href       = uk.gov.hmrc.cardpaymentfrontend.controllers.routes.AddressController.renderPage,
        linkId     = "check-your-details-card-billing-address-change-link",
        messageKey = "check-your-details.change"
      ))
    ))
  }

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
      case Origins.PfCt                     => ExtendedPfCt
      case Origins.PfEpayeNi                => DefaultExtendedOrigin
      case Origins.PfEpayeLpp               => DefaultExtendedOrigin
      case Origins.PfEpayeSeta              => DefaultExtendedOrigin
      case Origins.PfEpayeLateCis           => DefaultExtendedOrigin
      case Origins.PfEpayeP11d              => DefaultExtendedOrigin
      case Origins.PfSdlt                   => DefaultExtendedOrigin
      case Origins.PfCds                    => DefaultExtendedOrigin
      case Origins.PfOther                  => DefaultExtendedOrigin
      case Origins.PfP800                   => ExtendedPfP800
      case Origins.PtaP800                  => DefaultExtendedOrigin
      case Origins.PfClass2Ni               => DefaultExtendedOrigin
      case Origins.PfInsurancePremium       => DefaultExtendedOrigin
      case Origins.PfPsAdmin                => DefaultExtendedOrigin
      case Origins.BtaSa                    => ExtendedBtaSa
      case Origins.AppSa                    => DefaultExtendedOrigin
      case Origins.BtaVat                   => DefaultExtendedOrigin
      case Origins.BtaEpayeBill             => ExtendedBtaEpayeBill
      case Origins.BtaEpayePenalty          => DefaultExtendedOrigin
      case Origins.BtaEpayeInterest         => DefaultExtendedOrigin
      case Origins.BtaEpayeGeneral          => DefaultExtendedOrigin
      case Origins.BtaClass1aNi             => DefaultExtendedOrigin
      case Origins.BtaCt                    => ExtendedBtaCt
      case Origins.BtaSdil                  => DefaultExtendedOrigin
      case Origins.BcPngr                   => DefaultExtendedOrigin
      case Origins.Parcels                  => DefaultExtendedOrigin
      case Origins.DdVat                    => DefaultExtendedOrigin
      case Origins.DdSdil                   => DefaultExtendedOrigin
      case Origins.VcVatReturn              => DefaultExtendedOrigin
      case Origins.VcVatOther               => DefaultExtendedOrigin
      case Origins.ItSa                     => ExtendedItSa
      case Origins.Amls                     => DefaultExtendedOrigin
      case Origins.Ppt                      => DefaultExtendedOrigin
      case Origins.PfCdsCash                => DefaultExtendedOrigin
      case Origins.PfPpt                    => DefaultExtendedOrigin
      case Origins.PfSpiritDrinks           => DefaultExtendedOrigin
      case Origins.PfInheritanceTax         => DefaultExtendedOrigin
      case Origins.Mib                      => DefaultExtendedOrigin
      case Origins.PfClass3Ni               => DefaultExtendedOrigin
      case Origins.PtaSa                    => ExtendedPtaSa
      case Origins.PfWineAndCider           => DefaultExtendedOrigin
      case Origins.PfBioFuels               => DefaultExtendedOrigin
      case Origins.PfAirPass                => DefaultExtendedOrigin
      case Origins.PfMgd                    => DefaultExtendedOrigin
      case Origins.PfBeerDuty               => DefaultExtendedOrigin
      case Origins.PfGamingOrBingoDuty      => DefaultExtendedOrigin
      case Origins.PfGbPbRgDuty             => DefaultExtendedOrigin
      case Origins.PfLandfillTax            => DefaultExtendedOrigin
      case Origins.PfSdil                   => DefaultExtendedOrigin
      case Origins.PfAggregatesLevy         => DefaultExtendedOrigin
      case Origins.PfClimateChangeLevy      => DefaultExtendedOrigin
      case Origins.PfSimpleAssessment       => DefaultExtendedOrigin
      case Origins.PtaSimpleAssessment      => DefaultExtendedOrigin
      case Origins.AppSimpleAssessment      => DefaultExtendedOrigin
      case Origins.PfTpes                   => DefaultExtendedOrigin
      case Origins.CapitalGainsTax          => DefaultExtendedOrigin
      case Origins.EconomicCrimeLevy        => DefaultExtendedOrigin
      case Origins.PfEconomicCrimeLevy      => DefaultExtendedOrigin
      case Origins.PfJobRetentionScheme     => DefaultExtendedOrigin
      case Origins.JrsJobRetentionScheme    => DefaultExtendedOrigin
      case Origins.PfImportedVehicles       => DefaultExtendedOrigin
      case Origins.PfChildBenefitRepayments => DefaultExtendedOrigin
      case Origins.NiEuVatOss               => DefaultExtendedOrigin
      case Origins.PfNiEuVatOss             => DefaultExtendedOrigin
      case Origins.NiEuVatIoss              => DefaultExtendedOrigin
      case Origins.PfNiEuVatIoss            => DefaultExtendedOrigin
      case Origins.PfAmls                   => DefaultExtendedOrigin
      case Origins.PfAted                   => DefaultExtendedOrigin
      case Origins.PfCdsDeferment           => DefaultExtendedOrigin
      case Origins.PfTrust                  => DefaultExtendedOrigin
      case Origins.PtaClass3Ni              => DefaultExtendedOrigin
      case Origins.PfAlcoholDuty            => ExtendedPfAlcoholDuty
      case Origins.AlcoholDuty              => ExtendedAlcoholDuty
      case Origins.VatC2c                   => DefaultExtendedOrigin
      case Origins.PfVatC2c                 => DefaultExtendedOrigin
      case Origins.`3psSa`                  => DefaultExtendedOrigin
      case Origins.`3psVat`                 => DefaultExtendedOrigin
      case Origins.PfPillar2                => DefaultExtendedOrigin
      case Origins.Pillar2                  => DefaultExtendedOrigin
    }
  }
}
