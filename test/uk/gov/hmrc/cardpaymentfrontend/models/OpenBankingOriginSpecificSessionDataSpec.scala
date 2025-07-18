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

package uk.gov.hmrc.cardpaymentfrontend.models

import org.scalatest.AppendedClues.convertToClueful
import org.scalatest.Assertion
import payapi.corcommon.model.taxes.ad.{AlcoholDutyChargeReference, AlcoholDutyReference}
import payapi.corcommon.model.taxes.amls.AmlsPaymentReference
import payapi.corcommon.model.taxes.ct.{CtChargeTypes, CtPeriod, CtUtr}
import payapi.corcommon.model.taxes.epaye.{AccountsOfficeReference, EpayePenaltyReference, MonthlyEpayeTaxPeriod, PsaNumber, QuarterlyEpayeTaxPeriod, YearlyEpayeTaxPeriod}
import payapi.corcommon.model.taxes.other.{XRef, XRef14Char}
import payapi.corcommon.model.taxes.ppt.PptReference
import payapi.corcommon.model.taxes.sa.SaUtr
import payapi.corcommon.model.taxes.vat.{VatChargeReference, Vrn}
import payapi.corcommon.model.times.period.TaxQuarter.AprilJuly
import payapi.corcommon.model.times.period.{TaxMonth, TaxYear}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{TestHelpers, UnitSpec}

class OpenBankingOriginSpecificSessionDataSpec extends UnitSpec {

  private def testOsd(
      originSpecificSessionData: Option[OriginSpecificSessionData],
      expectedOsd:               OriginSpecificSessionData,
      expectedReference:         String,
      expectedSearchTag:         String
  ): Assertion = {
    originSpecificSessionData shouldBe Some(expectedOsd) withClue "osd didn't match"
    originSpecificSessionData.map(_.paymentReference.value) shouldBe Some(expectedReference) withClue "wrong paymentReference"
    originSpecificSessionData.map(_.searchTag.value) shouldBe Some(expectedSearchTag) withClue "wrong searchTag"
  }

  private def roundTripJsonTest(maybeThing: Option[OriginSpecificSessionData], expectedJson: JsValue): Assertion = {
    maybeThing.fold(throw new RuntimeException("expected some but was none")) { thing =>
      Json.toJson(thing) shouldBe expectedJson withClue "failed to write to json"
      Json.fromJson[OriginSpecificSessionData](expectedJson).asEither shouldBe Right(thing) withClue "failed to read from json"
    }
  }

  "Each extended origin should have a way to generate OriginSpecificSessionData for open banking" - {

    "PfSaSessionData" in {
      val testJson = Json.parse("""{"saUtr":"1234567895","origin":"PfSa"}""")
      val osd = ExtendedPfSa.openBankingOriginSpecificSessionData(TestJourneys.PfSa.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfSaSessionData(SaUtr("1234567895"), None), "1234567895K", "1234567895")
      roundTripJsonTest(osd, testJson)
    }

    "BtaSa" in {
      val testJson = Json.parse("""{"saUtr":"1234567895","origin":"BtaSa"}""")
      val osd = ExtendedBtaSa.openBankingOriginSpecificSessionData(TestJourneys.BtaSa.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaSaSessionData(SaUtr("1234567895"), None), "1234567895K", "1234567895")
      roundTripJsonTest(osd, testJson)
    }

    "PtaSa" in {
      val testJson = Json.parse("""{"saUtr":"1234567895","origin":"PtaSa"}""")
      val osd = ExtendedPtaSa.openBankingOriginSpecificSessionData(TestJourneys.PtaSa.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PtaSaSessionData(SaUtr("1234567895"), None), "1234567895K", "1234567895")
      roundTripJsonTest(osd, testJson)
    }

    "ItSa" in {
      val testJson = Json.parse("""{"saUtr":"1234567895","origin":"ItSa"}""")
      val osd = ExtendedItSa.openBankingOriginSpecificSessionData(TestJourneys.ItSa.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, ItSaSessionData(SaUtr("1234567895"), None), "1234567895K", "1234567895")
      roundTripJsonTest(osd, testJson)
    }

    "PfAlcoholDuty" in {
      val testJson = Json.parse("""{"alcoholDutyReference":"XMADP0123456789","origin":"PfAlcoholDuty"}""")
      val osd = ExtendedPfAlcoholDuty.openBankingOriginSpecificSessionData(TestJourneys.PfAlcoholDuty.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfAlcoholDutySessionData(AlcoholDutyReference("XMADP0123456789"), None), "XMADP0123456789", "XMADP0123456789")
      roundTripJsonTest(osd, testJson)
    }

    "AlcoholDuty" in {
      val testJson = Json.parse("""{"alcoholDutyReference":"XMADP0123456789","alcoholDutyChargeReference":"XE1234567890123","origin":"AlcoholDuty"}""")
      val osd = ExtendedAlcoholDuty.openBankingOriginSpecificSessionData(TestJourneys.AlcoholDuty.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, AlcoholDutySessionData(AlcoholDutyReference("XMADP0123456789"), Some(AlcoholDutyChargeReference("XE1234567890123")), None), "XE1234567890123", "XMADP0123456789")
      roundTripJsonTest(osd, testJson)
    }

    "PfCt" in {
      val testJson = Json.parse("""{"utr":"1097172564","ctPeriod":1,"ctChargeType":"A","origin":"PfCt"}""")
      val osd = ExtendedPfCt.openBankingOriginSpecificSessionData(TestJourneys.PfCt.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfCtSessionData(CtUtr("1097172564"), CtPeriod(1), CtChargeTypes.A, None), "1097172564A00101A", "1097172564")
      roundTripJsonTest(osd, testJson)
    }

    "BtaCt" in {
      val testJson = Json.parse("""{"utr":"1097172564","ctPeriod":1,"ctChargeType":"A","origin":"BtaCt"}""")
      val osd = ExtendedBtaCt.openBankingOriginSpecificSessionData(TestJourneys.BtaCt.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaCtSessionData(CtUtr("1097172564"), CtPeriod(1), CtChargeTypes.A, None), "1097172564A00101A", "1097172564")
      roundTripJsonTest(osd, testJson)
    }

    "PfEpayeNi" in {
      val testJson = Json.parse("""{"accountsOfficeReference":"123PH45678900","period":{"taxQuarter":"AprilJuly","taxYear":2025},"origin":"PfEpayeNi"}""")
      val osd = ExtendedPfEpayeNi.openBankingOriginSpecificSessionData(TestJourneys.PfEpayeNi.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfEpayeNiSessionData(AccountsOfficeReference("123PH45678900"), QuarterlyEpayeTaxPeriod(AprilJuly, TaxYear(2025)), None), "123PH456789002503", "123PH45678900")
      roundTripJsonTest(osd, testJson)
    }

    "PfEpayeLpp" in {
      val testJson = Json.parse("""{"payeInterestXRef":"XE123456789012","origin":"PfEpayeLpp"}""")
      val osd = ExtendedPfEpayeLpp.openBankingOriginSpecificSessionData(TestJourneys.PfEpayeLpp.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfEpayeLppSessionData(XRef("XE123456789012"), None), "XE123456789012", "XE123456789012")
      roundTripJsonTest(osd, testJson)
    }

    "PfEpayeLateCis" in {
      val testJson = Json.parse("""{"payeInterestXRef":"XE123456789012","origin":"PfEpayeLateCis"}""")
      val osd = ExtendedPfEpayeLateCis.openBankingOriginSpecificSessionData(TestJourneys.PfEpayeLateCis.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfEpayeLateCisSessionData(XRef14Char("XE123456789012"), None), "XE123456789012", "XE123456789012")
      roundTripJsonTest(osd, testJson)
    }

    "PfEpayeP11d" in {
      val testJson = Json.parse("""{"accountsOfficeReference":"123PH45678900","period":{"taxYear":2025},"origin":"PfEpayeP11d"}""")
      val osd = ExtendedPfEpayeP11d.openBankingOriginSpecificSessionData(TestJourneys.PfEpayeP11d.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfEpayeP11dSessionData(AccountsOfficeReference("123PH45678900"), YearlyEpayeTaxPeriod(TaxYear(2025)), None), "123PH456789002513", "123PH45678900")
      roundTripJsonTest(osd, testJson)
    }

    "PfEpayeSeta" in {
      val testJson = Json.parse("""{"psaNumber":"XA123456789012","origin":"PfEpayeSeta"}""")
      val osd = ExtendedPfEpayeSeta.openBankingOriginSpecificSessionData(TestJourneys.PfEpayeSeta.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfEpayeSetaSessionData(PsaNumber("XA123456789012"), None), "XA123456789012", "XA123456789012")
      roundTripJsonTest(osd, testJson)
    }

    "PfVat" in {
      val testJson = Json.parse("""{"vrn":"999964805","origin":"PfVat"}""")
      val osd = ExtendedPfVat.openBankingOriginSpecificSessionData(TestJourneys.PfVat.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfVatSessionData(Some(Vrn("999964805")), None, None), "999964805", "999964805")
      roundTripJsonTest(osd, testJson)
    }

    "BtaVat" in {
      val testJson = Json.parse("""{"vrn":"999964805","origin":"BtaVat"}""")
      val osd = ExtendedBtaVat.openBankingOriginSpecificSessionData(TestJourneys.BtaVat.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaVatSessionData(Vrn("999964805"), None), "999964805", "999964805")
      roundTripJsonTest(osd, testJson)
    }

    "VcVatReturn" in {
      val testJson = Json.parse("""{"vrn":"999964805","origin":"VcVatReturn"}""")
      val osd = ExtendedVcVatReturn.openBankingOriginSpecificSessionData(TestJourneys.VcVatReturn.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, VcVatReturnSessionData(Vrn("999964805"), None), "999964805", "999964805")
      roundTripJsonTest(osd, testJson)
    }

    "VcVatOther" in {
      val testJson = Json.parse("""{"vrn":"999964805","vatChargeReference":"999964805","origin":"VcVatOther"}""")
      val osd = ExtendedVcVatOther.openBankingOriginSpecificSessionData(TestJourneys.VcVatOther.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, VcVatOtherSessionData(Vrn("999964805"), VatChargeReference("999964805"), None), "999964805", "999964805")
      roundTripJsonTest(osd, testJson)
    }

    "Ppt" in {
      val testJson = Json.parse("""{"pptReference":"XAPPT0000012345","origin":"Ppt"}""")
      val osd = ExtendedPpt.openBankingOriginSpecificSessionData(TestJourneys.Ppt.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PptSessionData(PptReference("XAPPT0000012345"), None), "XAPPT0000012345", "XAPPT0000012345")
      roundTripJsonTest(osd, testJson)
    }

    "PfPpt" in {
      val testJson = Json.parse("""{"pptReference":"XAPPT0000012345","origin":"PfPpt"}""")
      val osd = ExtendedPfPpt.openBankingOriginSpecificSessionData(TestJourneys.PfPpt.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfPptSessionData(PptReference("XAPPT0000012345"), None), "XAPPT0000012345", "XAPPT0000012345")
      roundTripJsonTest(osd, testJson)
    }

    "BtaEpayeBill" in {
      val testJson = Json.parse("""{"accountsOfficeReference":"123PH45678900","period":{"taxMonth":"MayJune","taxYear":2027},"origin":"BtaEpayeBill"}""")
      val osd = ExtendedBtaEpayeBill.openBankingOriginSpecificSessionData(TestJourneys.BtaEpayeBill.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaEpayeBillSessionData(AccountsOfficeReference("123PH45678900"), MonthlyEpayeTaxPeriod(TaxMonth.MayJune, TaxYear(2027)), None), "123PH456789002702", "123PH45678900")
      roundTripJsonTest(osd, testJson)
    }

    "BtaEpayePenalty" in {
      val testJson = Json.parse("""{"epayePenaltyReference":"123PH45678900","origin":"BtaEpayePenalty"}""")
      val osd = ExtendedBtaEpayePenalty.openBankingOriginSpecificSessionData(TestJourneys.BtaEpayePenalty.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaEpayePenaltySessionData(EpayePenaltyReference("123PH45678900"), None), "123PH45678900", "123PH45678900")
      roundTripJsonTest(osd, testJson)
    }

    "BtaEpayeInterest" in {
      val testJson = Json.parse("""{"payeInterestXRef":"XE123456789012","origin":"BtaEpayeInterest"}""")
      val osd = ExtendedBtaEpayeInterest.openBankingOriginSpecificSessionData(TestJourneys.BtaEpayeInterest.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaEpayeInterestSessionData(XRef("XE123456789012"), None), "XE123456789012", "XE123456789012")
      roundTripJsonTest(osd, testJson)
    }

    "BtaEpayeGeneral" in {
      val testJson = Json.parse("""{"accountsOfficeReference":"123PH45678900","period":{"taxMonth":"MayJune","taxYear":2027},"origin":"BtaEpayeGeneral"}""")
      val osd = ExtendedBtaEpayeGeneral.openBankingOriginSpecificSessionData(TestJourneys.BtaEpayeGeneral.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaEpayeGeneralSessionData(AccountsOfficeReference("123PH45678900"), MonthlyEpayeTaxPeriod(TaxMonth.MayJune, TaxYear(2027)), None), "123PH456789002702", "123PH45678900")
      roundTripJsonTest(osd, testJson)
    }

    "BtaClass1aNi" in {
      val testJson = Json.parse("""{"accountsOfficeReference":"123PH45678900","period":{"taxYear":2027},"origin":"BtaClass1aNi"}""")
      val osd = ExtendedBtaClass1aNi.openBankingOriginSpecificSessionData(TestJourneys.BtaClass1aNi.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, BtaClass1aNiSessionData(AccountsOfficeReference("123PH45678900"), YearlyEpayeTaxPeriod(TaxYear(2027)), None), "123PH456789002713", "123PH45678900")
      roundTripJsonTest(osd, testJson)
    }

    "Amls" in {
      val testJson = Json.parse("""{"amlsPaymentReference":"XE123456789012","origin":"Amls"}""")
      val osd = ExtendedAmls.openBankingOriginSpecificSessionData(TestJourneys.Amls.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, AmlsSessionData(AmlsPaymentReference("XE123456789012"), None), "XE123456789012", "XE123456789012")
      roundTripJsonTest(osd, testJson)
    }

    "PfAmls" in {
      val testJson = Json.parse("""{"amlsPaymentReference":"XE123456789012","origin":"PfAmls"}""")
      val osd = ExtendedPfAmls.openBankingOriginSpecificSessionData(TestJourneys.PfAmls.journeyBeforeBeginWebPayment.journeySpecificData)
      testOsd(osd, PfAmlsSessionData(AmlsPaymentReference("XE123456789012"), None), "XE123456789012", "XE123456789012")
      roundTripJsonTest(osd, testJson)
    }

  }

  "sanity check for implemented origins" in {
    TestHelpers.implementedOrigins.size shouldBe 26 withClue "** This dummy test is here to remind you to update the tests above. Bump up the expected number when an origin is added to implemented origins **"
  }

}
