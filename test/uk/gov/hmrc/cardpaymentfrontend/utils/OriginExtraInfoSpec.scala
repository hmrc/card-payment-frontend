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
}

