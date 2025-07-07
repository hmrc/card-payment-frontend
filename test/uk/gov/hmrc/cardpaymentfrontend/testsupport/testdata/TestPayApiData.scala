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

package uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata

import payapi.corcommon.model.AmountInPence
import payapi.corcommon.model.taxes.ad.AlcoholDutyReference
import payapi.corcommon.model.taxes.ct.{CtChargeType, CtChargeTypes, CtPeriod, CtUtr}
import payapi.corcommon.model.taxes.epaye.{AccountsOfficeReference, MonthlyEpayeTaxPeriod, SubYearlyEpayeTaxPeriod, YearlyEpayeTaxPeriod}
import payapi.corcommon.model.taxes.other.{EconomicCrimeLevyReturnNumber, XRef, XRef14Char}
import payapi.corcommon.model.taxes.sa.SaUtr
import payapi.corcommon.model.taxes.vat.Vrn
import payapi.corcommon.model.times.period.{TaxMonth, TaxYear}

object TestPayApiData {
  val testSaUtr: SaUtr = SaUtr("1234567895")
  val testVrn: Vrn = Vrn("999964805")
  val testAlcoholDutyReference: AlcoholDutyReference = AlcoholDutyReference("XADP123456789")
  val testEconomicCrimeLevyRef: EconomicCrimeLevyReturnNumber = EconomicCrimeLevyReturnNumber("XADP123456789")
  val testXRef14Char: XRef14Char = XRef14Char("X123456789123")
  val testCtUtr: CtUtr = CtUtr("1234567895")
  val testCtPeriod: CtPeriod = CtPeriod(1)
  val testCtChargeType: CtChargeType = CtChargeTypes.A
  val testAccountsOfficeReference: AccountsOfficeReference = AccountsOfficeReference("123PH45678900")
  val testSubYearlyPeriod: SubYearlyEpayeTaxPeriod = MonthlyEpayeTaxPeriod(TaxMonth.MayJune, TaxYear(2027))
  val testYearlyPeriod: YearlyEpayeTaxPeriod = YearlyEpayeTaxPeriod(TaxYear(2027))
  val testXref: XRef = XRef("XE1234567890123")
  val testAmountInPence: AmountInPence = AmountInPence(12345)
  val testReturnUrl: Option[String] = Some("returnUrl")
}
