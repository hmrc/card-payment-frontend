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

import payapi.corcommon.model.Origins
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec

//Only a few tests here as this is intended to be replaced by a proper origin system in due course
class OriginExtraInfoSpec extends ItSpec {
  private val systemUnderTest: OriginExtraInfo = app.injector.instanceOf[OriginExtraInfo]

  "Open banking allowed should be true or false for various origins" in {
    systemUnderTest.openBankingAllowed(Origins.PfSa) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfSa) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfVat) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfCt) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfEpayeNi) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfEpayeLpp) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfEpayeSeta) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfEpayeLateCis) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfEpayeP11d) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfSdlt) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfCds) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfOther) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.PfP800) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.PfClass2Ni) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfInsurancePremium) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfPsAdmin) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfClass3Ni) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfBioFuels) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfAirPass) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BcPngr) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.PfMgd) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfBeerDuty) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfGamingOrBingoDuty) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfGbPbRgDuty) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfLandfillTax) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfSdil) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfAggregatesLevy) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfClimateChangeLevy) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PtaSimpleAssessment) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfSimpleAssessment) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfTpes) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaSa) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaVat) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaEpayeBill) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaEpayePenalty) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaEpayeInterest) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaEpayeGeneral) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaClass1aNi) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaCt) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.BtaSdil) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PtaSa) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.Parcels) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.Mib) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.DdVat) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.VcVatReturn) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.VcVatOther) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.ItSa) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.Amls) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.CapitalGainsTax) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.DdSdil) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.PfJobRetentionScheme) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.JrsJobRetentionScheme) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.PfChildBenefitRepayments) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.NiEuVatOss) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfNiEuVatOss) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfAmls) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.Ppt) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfPpt) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfInheritanceTax) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfWineAndCider) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfSpiritDrinks) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfImportedVehicles) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.AppSa) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfAted) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfCdsCash) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfCdsDeferment) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfTrust) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.EconomicCrimeLevy) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfEconomicCrimeLevy) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.AppSimpleAssessment) shouldBe false
    systemUnderTest.openBankingAllowed(Origins.NiEuVatIoss) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PfNiEuVatIoss) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.PtaClass3Ni) shouldBe true
    systemUnderTest.openBankingAllowed(Origins.AlcoholDuty) shouldBe true
  }

  "one off direct debit allowed should be true or false for various origins" in {
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfSa) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfVat) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfCt) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfEpayeNi) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfEpayeLpp) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfEpayeSeta) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfEpayeLateCis) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfEpayeP11d) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfSdlt) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfCds) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfOther) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfP800) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfClass2Ni) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfInsurancePremium) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfPsAdmin) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfClass3Ni) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfBioFuels) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfAirPass) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BcPngr) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfMgd) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfBeerDuty) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfGamingOrBingoDuty) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfGbPbRgDuty) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfLandfillTax) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfSdil) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfAggregatesLevy) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfClimateChangeLevy) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PtaSimpleAssessment) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfSimpleAssessment) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfTpes) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaSa) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaVat) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaEpayeBill) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaEpayePenalty) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaEpayeInterest) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaEpayeGeneral) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaClass1aNi) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaCt) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.BtaSdil) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PtaSa) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.Parcels) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.Mib) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.DdVat) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.VcVatReturn) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.VcVatOther) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.ItSa) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.Amls) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.CapitalGainsTax) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.DdSdil) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfJobRetentionScheme) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.JrsJobRetentionScheme) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfChildBenefitRepayments) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.NiEuVatOss) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfNiEuVatOss) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfAmls) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.Ppt) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfPpt) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfInheritanceTax) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfWineAndCider) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfSpiritDrinks) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfImportedVehicles) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.AppSa) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfAted) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfCdsCash) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfCdsDeferment) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfTrust) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.EconomicCrimeLevy) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfEconomicCrimeLevy) shouldBe true
    systemUnderTest.oneOffDirectDebitAllowed(Origins.AppSimpleAssessment) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.NiEuVatIoss) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PfNiEuVatIoss) shouldBe false
    systemUnderTest.oneOffDirectDebitAllowed(Origins.PtaClass3Ni) shouldBe true
  }

  "variable direct debit allowed should be true or false for various origins" in {
    systemUnderTest.variableDirectDebitAllowed(Origins.PfSa) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfVat) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.PfCt) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfEpayeNi) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfEpayeLpp) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfEpayeSeta) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfEpayeLateCis) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfEpayeP11d) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfSdlt) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfCds) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfOther) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfP800) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfClass2Ni) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfInsurancePremium) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfPsAdmin) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfClass3Ni) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfBioFuels) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfAirPass) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BcPngr) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfMgd) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.PfBeerDuty) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfGamingOrBingoDuty) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfGbPbRgDuty) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfLandfillTax) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfSdil) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.PfAggregatesLevy) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfClimateChangeLevy) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PtaSimpleAssessment) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfSimpleAssessment) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfTpes) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaSa) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaVat) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaEpayeBill) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaEpayePenalty) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaEpayeInterest) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaEpayeGeneral) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaClass1aNi) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaCt) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.BtaSdil) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.PtaSa) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.Parcels) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.Mib) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.DdVat) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.VcVatReturn) shouldBe true
    systemUnderTest.variableDirectDebitAllowed(Origins.VcVatOther) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.ItSa) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.Amls) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.CapitalGainsTax) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.DdSdil) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfJobRetentionScheme) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.JrsJobRetentionScheme) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfChildBenefitRepayments) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.NiEuVatOss) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfNiEuVatOss) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfAmls) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.Ppt) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfPpt) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfInheritanceTax) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfWineAndCider) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfSpiritDrinks) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfImportedVehicles) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.AppSa) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfAted) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfCdsCash) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfCdsDeferment) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfTrust) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.EconomicCrimeLevy) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfEconomicCrimeLevy) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.AppSimpleAssessment) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.NiEuVatIoss) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PfNiEuVatIoss) shouldBe false
    systemUnderTest.variableDirectDebitAllowed(Origins.PtaClass3Ni) shouldBe false
  }
}

