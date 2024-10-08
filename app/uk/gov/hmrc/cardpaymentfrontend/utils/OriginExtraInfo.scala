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

import payapi.corcommon.model.{Origin, Origins}

import javax.inject.Inject
trait PaymentMethod

final case class Card() extends PaymentMethod

final case class OpenBanking() extends PaymentMethod

final case class OneOffDirectDebit() extends PaymentMethod

final case class VariableDirectDebit() extends PaymentMethod

final case class PrintableDirectDebit() extends PaymentMethod

final case class Bacs() extends PaymentMethod

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
}

