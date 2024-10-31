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

import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.cgt.CgtAccountReference
import payapi.corcommon.model.taxes.amls.AmlsPaymentReference
import payapi.corcommon.model.taxes.cds.{CdsCashRef, CdsRef}
import payapi.corcommon.model.taxes.cdsd.CdsDefermentReference
import payapi.corcommon.model.taxes.epaye.{EpayePenaltyReference, PsaNumber}
import payapi.corcommon.model.taxes.ioss.Ioss
import payapi.corcommon.model.taxes.other._
import payapi.corcommon.model.taxes.p302.{P302ChargeRef, P302Ref}
import payapi.corcommon.model.taxes.ppt.PptReference
import payapi.corcommon.model.taxes.sd.SpiritDrinksReference
import payapi.corcommon.model.taxes.sdlt.Utrn
import payapi.corcommon.model.taxes.trusts.TrustReference
import payapi.corcommon.model.taxes.vat.{CalendarPeriod, VatChargeReference}
import payapi.corcommon.model.taxes.vatc2c.VatC2cReference
import payapi.corcommon.model.times.period.{CalendarQuarter, CalendarQuarterlyPeriod, TaxYear}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestPayApiData._
import uk.gov.hmrc.cardpaymentfrontend.util.OpenBankingUtils

class OpenBankingUtilsSpec extends ItSpec {

  val systemUnderTest: OpenBankingUtils = app.injector.instanceOf[OpenBankingUtils]

  "OpenBankingUtils" - {
    "originDataFromJsd" - {
      "should turn a Jsd into an Option[OriginSpecificSessionData]" - {
        "for JsdBtaSa" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaSa(testSaUtr, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(BtaSaSessionData(testSaUtr, testReturnUrl))
        }
        "for JsdPtaSa" in {
          val result = systemUnderTest.originDataFromJsd(JsdPtaSa(testSaUtr, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(PtaSaSessionData(testSaUtr, testReturnUrl))
        }
        "for JsdPfSa" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfSa(Some(testSaUtr)), testReturnUrl)
          result shouldBe Some(PfSaSessionData(testSaUtr, None))
        }
        "for JsdItSa" in {
          val result = systemUnderTest.originDataFromJsd(JsdItSa(testSaUtr, None), testReturnUrl)
          result shouldBe Some(ItSaSessionData(testSaUtr, None))
        }
        "for JsdBtaEpayeBill" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaEpayeBill(testAccountsOfficeReference, testSubYearlyPeriod, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(BtaEpayeBillSessionData(testAccountsOfficeReference, testSubYearlyPeriod, testReturnUrl))
        }
        "for JsdBtaEpayePenalty" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaEpayePenalty(EpayePenaltyReference("blah"), testAccountsOfficeReference, testSubYearlyPeriod, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(BtaEpayePenaltySessionData(EpayePenaltyReference("blah"), testReturnUrl))
        }
        "for JsdBtaEpayeInterest" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaEpayeInterest(testXref, testAccountsOfficeReference, testSubYearlyPeriod, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(BtaEpayeInterestSessionData(testXref, testReturnUrl))
        }
        "for JsdBtaEpayeGeneral" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaEpayeGeneral(testAccountsOfficeReference, None, Some(testSubYearlyPeriod)), testReturnUrl)
          result shouldBe Some(BtaEpayeGeneralSessionData(testAccountsOfficeReference, testSubYearlyPeriod, testReturnUrl))
        }
        "for JsdBtaClass1aNi" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaClass1aNi(testAccountsOfficeReference, testYearlyPeriod, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(BtaClass1aNiSessionData(testAccountsOfficeReference, testYearlyPeriod, testReturnUrl))
        }
        "for JsdBtaCt" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaCt(testCtUtr, Some(testCtPeriod), Some(testCtChargeType), testAmountInPence, None, None), testReturnUrl)
          result shouldBe Some(BtaCtSessionData(testCtUtr, testCtPeriod, testCtChargeType, testReturnUrl))
        }
        "for JsdBtaVat" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaVat(testVrn, None, testAmountInPence), testReturnUrl)
          result shouldBe Some(BtaVatSessionData(testVrn, testReturnUrl))
        }
        "for JsdVcVatReturn" in {
          val result = systemUnderTest.originDataFromJsd(JsdVcVatReturn(testVrn, None, None, testAmountInPence), testReturnUrl)
          result shouldBe Some(VcVatReturnSessionData(testVrn, testReturnUrl))
        }
        "for JsdVcVatOther" in {
          val result = systemUnderTest.originDataFromJsd(JsdVcVatOther(testVrn, VatChargeReference("1234"), None, None, testAmountInPence), testReturnUrl)
          result shouldBe Some(VcVatOtherSessionData(testVrn, VatChargeReference("1234"), testReturnUrl))
        }
        "for JsdPfVat" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfVat(Some(testVrn), None), testReturnUrl)
          result shouldBe Some(PfVatSessionData(Some(testVrn), None, None))
        }
        "for JsdPfCt" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfCt(Some(testCtUtr), Some(testCtPeriod), Some(testCtChargeType)), testReturnUrl)
          result shouldBe Some(PfCtSessionData(testCtUtr, testCtPeriod, testCtChargeType, None))
        }
        "for JsdPfEpayeNi" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfEpayeNi(Some(testAccountsOfficeReference), Some(testSubYearlyPeriod)), testReturnUrl)
          result shouldBe Some(PfEpayeNiSessionData(testAccountsOfficeReference, testSubYearlyPeriod, None))
        }
        "for JsdPfEpayeLpp" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfEpayeLpp(Some(testXref)), testReturnUrl)
          result shouldBe Some(PfEpayeLppSessionData(testXref, None))
        }
        "for JsdPfEpayeSeta" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfEpayeSeta(Some(PsaNumber("123456"))), testReturnUrl)
          result shouldBe Some(PfEpayeSetaSessionData(PsaNumber("123456"), None))
        }
        "for JsdPfEpayeLateCis" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfEpayeLateCis(Some(testXRef14Char)), testReturnUrl)
          result shouldBe Some(PfEpayeLateCisSessionData(testXRef14Char, None))
        }
        "for JsdPfEpayeP11d" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfEpayeP11d(Some(testAccountsOfficeReference), Some(testYearlyPeriod)), testReturnUrl)
          result shouldBe Some(PfEpayeP11dSessionData(testAccountsOfficeReference, testYearlyPeriod))
        }
        "for JsdNiEuVatOss" in {
          val result = systemUnderTest.originDataFromJsd(JsdNiEuVatOss(testVrn, CalendarQuarterlyPeriod(CalendarQuarter.JanuaryToMarch, 2027), testAmountInPence, None), testReturnUrl)
          result shouldBe Some(NiEuVatOssSessionData(testVrn, CalendarQuarterlyPeriod(CalendarQuarter.JanuaryToMarch, 2027), Some("http://localhost:10204/pay-vat-on-goods-sold-to-eu/northern-ireland-returns-payments/your-account")))
        }
        "for JsdNiEuVatIoss" in {
          val result = systemUnderTest.originDataFromJsd(JsdNiEuVatIoss(Ioss("123456"), CalendarPeriod(1, 2027), testAmountInPence, None), testReturnUrl)
          result shouldBe Some(NiEuVatIossSessionData(Ioss("123456"), CalendarPeriod(1, 2027), Some("http://localhost:10193/pay-vat-on-goods-sold-to-eu/import-one-stop-shop-returns-payments/your-account")))
        }
        "for JsdPfNiEuVatOss" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfNiEuVatOss(Some(testVrn), Some(CalendarQuarterlyPeriod(CalendarQuarter.JanuaryToMarch, 2027))), testReturnUrl)
          result shouldBe Some(PfNiEuVatOssSessionData(testVrn, CalendarQuarterlyPeriod(CalendarQuarter.JanuaryToMarch, 2027), None))
        }
        "for JsdPfNiEuVatIoss" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfNiEuVatIoss(Some(Ioss("123456")), Some(CalendarPeriod(1, 2027))), testReturnUrl)
          result shouldBe Some(PfNiEuVatIossSessionData(Ioss("123456"), CalendarPeriod(1, 2027), None))
        }
        "for JsdCapitalGainsTax" in {
          val result = systemUnderTest.originDataFromJsd(JsdCapitalGainsTax(CgtAccountReference("1234"), None, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(CapitalGainsTaxSessionData(CgtAccountReference("1234"), testReturnUrl))
        }
        "for JsdPtaSimpleAssessment" in {
          val result = systemUnderTest.originDataFromJsd(JsdPtaSimpleAssessment(P302Ref("12345"), P302ChargeRef("12345"), TaxYear(2027), testAmountInPence, None), testReturnUrl)
          result shouldBe Some(PtaSimpleAssessmentSessionData(P302Ref("12345"), P302ChargeRef("12345"), testReturnUrl))
        }
        "for JsdPfSimpleAssessment" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfSimpleAssessment(Some(testXRef14Char)), testReturnUrl)
          result shouldBe Some(PfSimpleAssessmentSessionData(testXRef14Char, None))
        }
        "for JsdPfSdlt" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfSdlt(Some(Utrn("12345"))), testReturnUrl)
          result shouldBe Some(PfSdltSessionData(Utrn("12345"), None))
        }
        "for JsdPfBioFuels" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfBioFuels(Some(BioFuelsRegistrationNumber("12345"))), testReturnUrl)
          result shouldBe Some(PfBioFuelsSessionData(BioFuelsRegistrationNumber("12345"), None))
        }
        "for JsdPfGbPbRgDuty" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfGbPbRgDuty(Some(testXRef14Char)), testReturnUrl)
          result shouldBe Some(PfGbPbRgDutySessionData(testXRef14Char, None))
        }
        "for JsdPfMgd" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfMgd(Some(testXRef14Char)), testReturnUrl)
          result shouldBe Some(PfMgdSessionData(testXRef14Char, None))
        }
        "for JsdPfGamingOrBingoDuty" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfGamingOrBingoDuty(Some(GamingOrBingoDutyRef("X1234567890123"))), testReturnUrl)
          result shouldBe Some(PfGamingOrBingoDutySessionData(testXref, None))
        }
        "for JsdPfAmls" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfAmls(Some(AmlsPaymentReference("1234"))), testReturnUrl)
          result shouldBe Some(PfAmlsSessionData(AmlsPaymentReference("1234"), None))
        }
        "for JsdAmls" in {
          val result = systemUnderTest.originDataFromJsd(JsdAmls(AmlsPaymentReference("1234"), testAmountInPence), testReturnUrl)
          result shouldBe Some(AmlsSessionData(AmlsPaymentReference("1234"), testReturnUrl))
        }
        "for JsdPfTpes" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfTpes(Some(testXref)), testReturnUrl)
          result shouldBe Some(PfTpesSessionData(testXref, None))
        }
        "for JsdPfChildBenefitRepayments" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfChildBenefitRepayments(Some(YRef("Y123456789"))), testReturnUrl)
          result shouldBe Some(PfChildBenefitSessionData(YRef("Y123456789"), None))
        }
        "for JsdPfAggregatesLevy" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfAggregatesLevy(Some(AggregatesLevyRef("12345"))), testReturnUrl)
          result shouldBe Some(PfAggregatesLevySessionData(AggregatesLevyRef("12345"), None))
        }
        "for JsdPfLandfillTax" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfLandfillTax(Some(LandfillTaxRef("X1234567890123"))), testReturnUrl)
          result shouldBe Some(PfLandfillTaxSessionData(testXref, None))
        }
        "for JsdPfClimateChangeLevy" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfClimateChangeLevy(Some(ClimateChangeLevyRef("1234"))), testReturnUrl)
          result shouldBe Some(PfClimateChangeLevySessionData(ClimateChangeLevyRef("1234"), None))
        }
        "for JsdPfCds" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfCds(Some(CdsRef("1234")), 2027), testReturnUrl)
          result shouldBe Some(PfCdsSessionData(CdsRef("1234"), None))
        }
        "for JsdPfInsurancePremium" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfInsurancePremium(Some(InsurancePremiumRef("1234"))), testReturnUrl)
          result shouldBe Some(PfInsurancePremiumSessionData(InsurancePremiumRef("1234"), None))
        }
        "for JsdPfAirPass" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfAirPass(Some(AirPassReference("1234"))), testReturnUrl)
          result shouldBe Some(PfAirPassSessionData(AirPassReference("1234"), None))
        }
        "for JsdPfClass2Ni" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfClass2Ni(Some(Class2NiReference("2345"))), testReturnUrl)
          result shouldBe Some(PfClass2NiSessionData(Class2NiReference("2345"), None))
        }
        "for JsdPfBeerDuty" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfBeerDuty(Some(BeerDutyRef("1234"))), testReturnUrl)
          result shouldBe Some(PfBeerDutySessionData(BeerDutyRef("1234"), None))
        }
        "for JsdPfPsAdmin" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfPsAdmin(Some(testXref)), testReturnUrl)
          result shouldBe Some(PfPsAdminTaxSessionData(testXref, None))
        }
        "for JsdPfClass3Ni" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfClass3Ni(Some(Class3NiRef("1234"))), testReturnUrl)
          result shouldBe Some(PfClass3NiSessionData(Class3NiRef("1234"), None))
        }
        "for JsdPpt" in {
          val result = systemUnderTest.originDataFromJsd(JsdPpt(PptReference("1234"), testAmountInPence, None), testReturnUrl)
          result shouldBe Some(PptSessionData(PptReference("1234"), testReturnUrl))
        }
        "for JsdPfPpt" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfPpt(Some(PptReference("1234"))), testReturnUrl)
          result shouldBe Some(PfPptSessionData(PptReference("1234"), None))
        }
        "for JsdPfSdil" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfSdil(Some(SoftDrinksIndustryLevyRef("123456"))), testReturnUrl)
          result shouldBe Some(PfSdilSessionData(SoftDrinksIndustryLevyRef("123456"), None))
        }
        "for JsdBtaSdil" in {
          val result = systemUnderTest.originDataFromJsd(JsdBtaSdil(testXref, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(BtaSdilSessionData(testXref, testReturnUrl))
        }
        "for JsdPfInheritanceTax" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfInheritanceTax(Some(InheritanceTaxRef("123456"))), testReturnUrl)
          result shouldBe Some(PfInheritanceTaxSessionData(InheritanceTaxRef("123456")))
        }
        "for JsdPfWineAndCider" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfWineAndCider(Some(WineAndCiderTaxRef("123456"))), testReturnUrl)
          result shouldBe Some(PfWineAndCiderTaxSessionData(WineAndCiderTaxRef("123456")))
        }
        "for JsdPfSpiritsDrinks" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfSpiritsDrinks(Some(SpiritDrinksReference("123456"))), testReturnUrl)
          result shouldBe Some(PfSpiritDrinksSessionData(SpiritDrinksReference("123456"), None))
        }
        "for JsdPfImportedVehicles" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfImportedVehicles(Some(ImportedVehiclesRef("124556"))), testReturnUrl)
          result shouldBe Some(PfImportedVehiclesSessionData(ImportedVehiclesRef("124556")))
        }
        "for JsdAppSa" in {
          val result = systemUnderTest.originDataFromJsd(JsdAppSa(testSaUtr, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(AppSaSessionData(testSaUtr, testReturnUrl))
        }
        "for JsdPfAted" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfAted(Some(testXref)), testReturnUrl)
          result shouldBe Some(PfAtedSessionData(testXref, testReturnUrl))
        }
        "for JsdPfCdsDeferment" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfCdsDeferment(Some(CdsDefermentReference("123456"))), testReturnUrl)
          result shouldBe Some(PfCdsDefermentSessionData(CdsDefermentReference("123456"), testReturnUrl))
        }
        "for JsdPfCdsCash" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfCdsCash(Some(CdsCashRef("123456"))), testReturnUrl)
          result shouldBe Some(PfCdsCashSessionData(CdsCashRef("123456"), testReturnUrl))
        }
        "for JsdPfTrust" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfTrust(Some(TrustReference("1234"))), testReturnUrl)
          result shouldBe Some(PfTrustSessionData(TrustReference("1234"), testReturnUrl))
        }
        "for JsdPfEconomicCrimeLevy" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfEconomicCrimeLevy(Some(testEconomicCrimeLevyRef)), testReturnUrl)
          result shouldBe Some(PfEconomicCrimeLevySessionData(testEconomicCrimeLevyRef, testReturnUrl))
        }
        "for JsdEconomicCrimeLevy" in {
          val result = systemUnderTest.originDataFromJsd(JsdEconomicCrimeLevy(testEconomicCrimeLevyRef, testAmountInPence, None), testReturnUrl)
          result shouldBe Some(EconomicCrimeLevySessionData(testEconomicCrimeLevyRef, testReturnUrl))
        }
        "for JsdPtaClass3Ni" in {
          val result = systemUnderTest.originDataFromJsd(JsdPtaClass3Ni(Class3NiRef("1234567"), testAmountInPence), testReturnUrl)
          result shouldBe Some(PtaClass3NiSessionData(Class3NiRef("1234567"), testReturnUrl))
        }
        "for JsdPfAlcoholDuty" in {
          val result = systemUnderTest.originDataFromJsd(JsdPfAlcoholDuty(Some(testAlcoholDutyReference)), testReturnUrl)
          result shouldBe Some(PfAlcoholDutySessionData(testAlcoholDutyReference, testReturnUrl))
        }
        "for JsdAlcoholDuty" in {
          val result = systemUnderTest.originDataFromJsd(JsdAlcoholDuty(testAlcoholDutyReference, None, testAmountInPence), testReturnUrl)
          result shouldBe Some(AlcoholDutySessionData(testAlcoholDutyReference, None, testReturnUrl))
        }
        "for JsdVatC2c" in {
          val result = systemUnderTest.originDataFromJsd(JsdVatC2c(VatC2cReference("blah"), testAmountInPence), testReturnUrl)
          result shouldBe Some(VatC2cSessionData(VatC2cReference("blah"), None))
        }
      }

      "should turn a Jsd into an Option[OriginSpecificSessionData] with /business-account as the return url when return url provided is empty for relevant origins" in {
        val businessAccountDefaultUrl: String = "/business-account"
        systemUnderTest.originDataFromJsd(JsdBtaSa(testSaUtr, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaSa"
        systemUnderTest.originDataFromJsd(JsdBtaEpayeBill(testAccountsOfficeReference, testSubYearlyPeriod, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaEpayeBill"
        systemUnderTest.originDataFromJsd(JsdBtaEpayePenalty(EpayePenaltyReference("blah"), testAccountsOfficeReference, testSubYearlyPeriod, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaEpayePenalty"
        systemUnderTest.originDataFromJsd(JsdBtaEpayeInterest(testXref, testAccountsOfficeReference, testSubYearlyPeriod, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaEpayeInterest"
        systemUnderTest.originDataFromJsd(JsdBtaEpayeGeneral(testAccountsOfficeReference, None, Some(testSubYearlyPeriod)), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaEpayeGeneral"
        systemUnderTest.originDataFromJsd(JsdBtaClass1aNi(testAccountsOfficeReference, testYearlyPeriod, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaClass1aNi"
        systemUnderTest.originDataFromJsd(JsdBtaCt(testCtUtr, Some(testCtPeriod), Some(testCtChargeType), testAmountInPence, None, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdBtaCt"
        systemUnderTest.originDataFromJsd(JsdAppSa(testSaUtr, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdAppSa"
        systemUnderTest.originDataFromJsd(JsdPfAted(Some(testXref)), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPfAted"
        systemUnderTest.originDataFromJsd(JsdPfCdsDeferment(Some(CdsDefermentReference("123456"))), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPfCdsDeferment"
        systemUnderTest.originDataFromJsd(JsdPfCdsCash(Some(CdsCashRef("123456"))), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPfCdsCash"
        systemUnderTest.originDataFromJsd(JsdPfTrust(Some(TrustReference("1234"))), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPfTrust"
        systemUnderTest.originDataFromJsd(JsdPfEconomicCrimeLevy(Some(testEconomicCrimeLevyRef)), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPfEconomicCrimeLevy"
        systemUnderTest.originDataFromJsd(JsdEconomicCrimeLevy(testEconomicCrimeLevyRef, testAmountInPence, None), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdEconomicCrimeLevy"
        systemUnderTest.originDataFromJsd(JsdPtaClass3Ni(Class3NiRef("1234567"), testAmountInPence), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPtaClass3Ni"
        systemUnderTest.originDataFromJsd(JsdPfAlcoholDuty(Some(testAlcoholDutyReference)), Some("")).flatMap(_.returnUrl) shouldBe Some(businessAccountDefaultUrl) withClue "test failed for origin: JsdPfAlcoholDuty"
      }
    }
  }

}
