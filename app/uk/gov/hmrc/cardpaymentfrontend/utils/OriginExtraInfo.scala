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

package uk.gov.hmrc.cardpaymentfrontend.utils

import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins.{DefaultExtendedOrigin, ExtendedOrigin, ExtendedPfP800, ExtendedPfSa, ExtendedPfVat}
import payapi.corcommon.model.{Origin, Origins}

import javax.inject.{Inject, Singleton}
trait PaymentMethod

final case class Card() extends PaymentMethod

final case class OpenBanking() extends PaymentMethod

final case class OneOffDirectDebit() extends PaymentMethod

final case class VariableDirectDebit() extends PaymentMethod

final case class PrintableDirectDebit() extends PaymentMethod

final case class Bacs() extends PaymentMethod

//Probably a temporary class - it will be subsumed by the ExtendedOrigins in due course

@Singleton
class OriginExtraInfo @Inject() () {
  def paymentMethod(origin: Origin): Set[PaymentMethod] = {
    origin match {
      case Origins.PfSa                     => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfVat                    => Set(Card(), OpenBanking(), VariableDirectDebit(), Bacs())
      case Origins.PfCt                     => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfEpayeNi                => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfEpayeLpp               => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfEpayeSeta              => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfEpayeLateCis           => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfEpayeP11d              => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfSdlt                   => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfCds                    => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfOther                  => Set(Card(), Bacs())
      case Origins.PfP800                   => Set(Card(), Bacs())
      case Origins.PtaP800                  => Set(Card(), Bacs())
      case Origins.PfClass2Ni               => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfInsurancePremium       => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfPsAdmin                => Set(Card(), OpenBanking(), Bacs())
      case Origins.BtaSa                    => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.AppSa                    => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.BtaVat                   => Set(Card(), OpenBanking(), VariableDirectDebit(), Bacs())
      case Origins.BtaEpayeBill             => Set(Card(), OpenBanking(), OneOffDirectDebit(), VariableDirectDebit(), Bacs())
      case Origins.BtaEpayePenalty          => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.BtaEpayeInterest         => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.BtaEpayeGeneral          => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.BtaClass1aNi             => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.BtaCt                    => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.BtaSdil                  => Set(Card(), OpenBanking(), VariableDirectDebit(), Bacs())
      case Origins.BcPngr                   => Set(Card(), Bacs())
      case Origins.Parcels                  => Set.empty[PaymentMethod]
      case Origins.DdVat                    => Set.empty[PaymentMethod]
      case Origins.DdSdil                   => Set.empty[PaymentMethod]
      case Origins.VcVatReturn              => Set(Card(), OpenBanking(), VariableDirectDebit(), Bacs())
      case Origins.VcVatOther               => Set(Card(), OpenBanking(), Bacs())
      case Origins.ItSa                     => Set.empty[PaymentMethod]
      case Origins.Amls                     => Set(Card(), OpenBanking(), Bacs())
      case Origins.Ppt                      => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfCdsCash                => Set(OpenBanking(), Bacs())
      case Origins.PfPpt                    => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfSpiritDrinks           => Set(OpenBanking(), Bacs())
      case Origins.PfInheritanceTax         => Set(OpenBanking(), Bacs())
      case Origins.Mib                      => Set(Card(), Bacs())
      case Origins.PfClass3Ni               => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PtaSa                    => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfWineAndCider           => Set(OpenBanking(), Bacs())
      case Origins.PfBioFuels               => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfAirPass                => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfMgd                    => Set(Card(), OpenBanking(), OneOffDirectDebit(), VariableDirectDebit(), Bacs())
      case Origins.PfBeerDuty               => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfGamingOrBingoDuty      => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfGbPbRgDuty             => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfLandfillTax            => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfSdil                   => Set(Card(), OpenBanking(), VariableDirectDebit(), Bacs())
      case Origins.PfAggregatesLevy         => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfClimateChangeLevy      => Set(OpenBanking(), PrintableDirectDebit(), Bacs())
      case Origins.PfSimpleAssessment       => Set(Card(), OpenBanking(), Bacs())
      case Origins.PtaSimpleAssessment      => Set(Card(), OpenBanking(), Bacs())
      case Origins.AppSimpleAssessment      => Set(Card(), Bacs())
      case Origins.PfTpes                   => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.CapitalGainsTax          => Set(Card(), OpenBanking(), Bacs())
      case Origins.EconomicCrimeLevy        => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfEconomicCrimeLevy      => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfJobRetentionScheme     => Set(Card(), OneOffDirectDebit(), Bacs())
      case Origins.JrsJobRetentionScheme    => Set(Card(), OneOffDirectDebit(), Bacs())
      case Origins.PfImportedVehicles       => Set(OpenBanking(), Bacs())
      case Origins.PfChildBenefitRepayments => Set(Card(), OpenBanking(), Bacs())
      case Origins.NiEuVatOss               => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfNiEuVatOss             => Set(Card(), OpenBanking(), Bacs())
      case Origins.NiEuVatIoss              => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfNiEuVatIoss            => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfAmls                   => Set(Card(), OpenBanking(), Bacs())
      case Origins.PfAted                   => Set(OpenBanking(), Bacs())
      case Origins.PfCdsDeferment           => Set(OpenBanking(), Bacs())
      case Origins.PfTrust                  => Set(Card(), OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PtaClass3Ni              => Set(OpenBanking(), OneOffDirectDebit(), Bacs())
      case Origins.PfAlcoholDuty            => Set(Card(), OpenBanking(), Bacs()) // DD also allowed but not in Payments
      case Origins.AlcoholDuty              => Set(Card(), OpenBanking(), Bacs()) // DD also allowed but not in Payments
    }

  }

  def openBankingAllowed(origin: Origin): Boolean = paymentMethod(origin).contains(OpenBanking())

  def oneOffDirectDebitAllowed(origin: Origin): Boolean = paymentMethod(origin).contains(OneOffDirectDebit())

  def variableDirectDebitAllowed(origin: Origin): Boolean = paymentMethod(origin).contains(VariableDirectDebit())

  //  def lift(origin: Origin): ExtendedOrigin = {
  //    origin match {
  //      case Origins.PfSa => new ExtendedPfSa()
  //      case _            => new DefaultExtendedOrigin()
  //    }
  //  }

  def lift(origin: Origin): ExtendedOrigin = {
    origin match {
      case Origins.PfSa                     => new ExtendedPfSa()
      case Origins.PfVat                    => new ExtendedPfVat()
      case Origins.PfCt                     => new DefaultExtendedOrigin()
      case Origins.PfEpayeNi                => new DefaultExtendedOrigin()
      case Origins.PfEpayeLpp               => new DefaultExtendedOrigin()
      case Origins.PfEpayeSeta              => new DefaultExtendedOrigin()
      case Origins.PfEpayeLateCis           => new DefaultExtendedOrigin()
      case Origins.PfEpayeP11d              => new DefaultExtendedOrigin()
      case Origins.PfSdlt                   => new DefaultExtendedOrigin()
      case Origins.PfCds                    => new DefaultExtendedOrigin()
      case Origins.PfOther                  => new DefaultExtendedOrigin()
      case Origins.PfP800                   => new ExtendedPfP800()
      case Origins.PtaP800                  => new DefaultExtendedOrigin()
      case Origins.PfClass2Ni               => new DefaultExtendedOrigin()
      case Origins.PfInsurancePremium       => new DefaultExtendedOrigin()
      case Origins.PfPsAdmin                => new DefaultExtendedOrigin()
      case Origins.BtaSa                    => new DefaultExtendedOrigin()
      case Origins.AppSa                    => new DefaultExtendedOrigin()
      case Origins.BtaVat                   => new DefaultExtendedOrigin()
      case Origins.BtaEpayeBill             => new DefaultExtendedOrigin()
      case Origins.BtaEpayePenalty          => new DefaultExtendedOrigin()
      case Origins.BtaEpayeInterest         => new DefaultExtendedOrigin()
      case Origins.BtaEpayeGeneral          => new DefaultExtendedOrigin()
      case Origins.BtaClass1aNi             => new DefaultExtendedOrigin()
      case Origins.BtaCt                    => new DefaultExtendedOrigin()
      case Origins.BtaSdil                  => new DefaultExtendedOrigin()
      case Origins.BcPngr                   => new DefaultExtendedOrigin()
      case Origins.Parcels                  => new DefaultExtendedOrigin()
      case Origins.DdVat                    => new DefaultExtendedOrigin()
      case Origins.DdSdil                   => new DefaultExtendedOrigin()
      case Origins.VcVatReturn              => new DefaultExtendedOrigin()
      case Origins.VcVatOther               => new DefaultExtendedOrigin()
      case Origins.ItSa                     => new DefaultExtendedOrigin()
      case Origins.Amls                     => new DefaultExtendedOrigin()
      case Origins.Ppt                      => new DefaultExtendedOrigin()
      case Origins.PfCdsCash                => new DefaultExtendedOrigin()
      case Origins.PfPpt                    => new DefaultExtendedOrigin()
      case Origins.PfSpiritDrinks           => new DefaultExtendedOrigin()
      case Origins.PfInheritanceTax         => new DefaultExtendedOrigin()
      case Origins.Mib                      => new DefaultExtendedOrigin()
      case Origins.PfClass3Ni               => new DefaultExtendedOrigin()
      case Origins.PtaSa                    => new DefaultExtendedOrigin()
      case Origins.PfWineAndCider           => new DefaultExtendedOrigin()
      case Origins.PfBioFuels               => new DefaultExtendedOrigin()
      case Origins.PfAirPass                => new DefaultExtendedOrigin()
      case Origins.PfMgd                    => new DefaultExtendedOrigin()
      case Origins.PfBeerDuty               => new DefaultExtendedOrigin()
      case Origins.PfGamingOrBingoDuty      => new DefaultExtendedOrigin()
      case Origins.PfGbPbRgDuty             => new DefaultExtendedOrigin()
      case Origins.PfLandfillTax            => new DefaultExtendedOrigin()
      case Origins.PfSdil                   => new DefaultExtendedOrigin()
      case Origins.PfAggregatesLevy         => new DefaultExtendedOrigin()
      case Origins.PfClimateChangeLevy      => new DefaultExtendedOrigin()
      case Origins.PfSimpleAssessment       => new DefaultExtendedOrigin()
      case Origins.PtaSimpleAssessment      => new DefaultExtendedOrigin()
      case Origins.AppSimpleAssessment      => new DefaultExtendedOrigin()
      case Origins.PfTpes                   => new DefaultExtendedOrigin()
      case Origins.CapitalGainsTax          => new DefaultExtendedOrigin()
      case Origins.EconomicCrimeLevy        => new DefaultExtendedOrigin()
      case Origins.PfEconomicCrimeLevy      => new DefaultExtendedOrigin()
      case Origins.PfJobRetentionScheme     => new DefaultExtendedOrigin()
      case Origins.JrsJobRetentionScheme    => new DefaultExtendedOrigin()
      case Origins.PfImportedVehicles       => new DefaultExtendedOrigin()
      case Origins.PfChildBenefitRepayments => new DefaultExtendedOrigin()
      case Origins.NiEuVatOss               => new DefaultExtendedOrigin()
      case Origins.PfNiEuVatOss             => new DefaultExtendedOrigin()
      case Origins.NiEuVatIoss              => new DefaultExtendedOrigin()
      case Origins.PfNiEuVatIoss            => new DefaultExtendedOrigin()
      case Origins.PfAmls                   => new DefaultExtendedOrigin()
      case Origins.PfAted                   => new DefaultExtendedOrigin()
      case Origins.PfCdsDeferment           => new DefaultExtendedOrigin()
      case Origins.PfTrust                  => new DefaultExtendedOrigin()
      case Origins.PtaClass3Ni              => new DefaultExtendedOrigin()
      case Origins.PfAlcoholDuty            => new DefaultExtendedOrigin()
      case Origins.AlcoholDuty              => new DefaultExtendedOrigin()
    }
  }
}

