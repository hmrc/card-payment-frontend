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

package uk.gov.hmrc.cardpaymentfrontend.util

import org.scalatest.AppendedClues.convertToClueful
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor3}
import payapi.corcommon.model.taxes.epaye.{FixedLengthEpayeTaxPeriod, MonthlyEpayeTaxPeriod, QuarterlyEpayeTaxPeriod, YearlyEpayeTaxPeriod}
import payapi.corcommon.model.taxes.vat.CalendarPeriod
import payapi.corcommon.model.times.period.TaxMonth.*
import payapi.corcommon.model.times.period.TaxQuarter.{AprilJuly, JanuaryApril, JulyOctober, OctoberJanuary}
import payapi.corcommon.model.times.period.{CalendarQuarter, CalendarQuarterlyPeriod, TaxYear}
import play.api.i18n.Lang
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

class PeriodSpec extends UnitSpec with TableDrivenPropertyChecks {

  "humanReadablePeriod" - {
    "should return correct representation" in {
      val testCases: TableFor3[FixedLengthEpayeTaxPeriod, Lang, String] = Table(
        ("period", "lang", "expectedResult"),
        (YearlyEpayeTaxPeriod(TaxYear(2025)), Lang("en"), "2024 to 2025"),
        (YearlyEpayeTaxPeriod(TaxYear(2025)), Lang("cy"), "2024 i 2025"),
        (MonthlyEpayeTaxPeriod(JanuaryFebruary, TaxYear(2025)), Lang("en"), "6 January 2025 to 5 February 2025 (month 10)"),
        (MonthlyEpayeTaxPeriod(FebruaryMarch, TaxYear(2025)), Lang("en"), "6 February 2025 to 5 March 2025 (month 11)"),
        (MonthlyEpayeTaxPeriod(MarchApril, TaxYear(2025)), Lang("en"), "6 March 2025 to 5 April 2025 (month 12)"),
        (MonthlyEpayeTaxPeriod(AprilMay, TaxYear(2025)), Lang("en"), "6 April 2024 to 5 May 2024 (month 1)"),
        (MonthlyEpayeTaxPeriod(MayJune, TaxYear(2025)), Lang("en"), "6 May 2024 to 5 June 2024 (month 2)"),
        (MonthlyEpayeTaxPeriod(JuneJuly, TaxYear(2025)), Lang("en"), "6 June 2024 to 5 July 2024 (month 3)"),
        (MonthlyEpayeTaxPeriod(JulyAugust, TaxYear(2025)), Lang("en"), "6 July 2024 to 5 August 2024 (month 4)"),
        (MonthlyEpayeTaxPeriod(AugustSeptember, TaxYear(2025)), Lang("en"), "6 August 2024 to 5 September 2024 (month 5)"),
        (MonthlyEpayeTaxPeriod(SeptemberOctober, TaxYear(2025)), Lang("en"), "6 September 2024 to 5 October 2024 (month 6)"),
        (MonthlyEpayeTaxPeriod(OctoberNovember, TaxYear(2025)), Lang("en"), "6 October 2024 to 5 November 2024 (month 7)"),
        (MonthlyEpayeTaxPeriod(NovemberDecember, TaxYear(2025)), Lang("en"), "6 November 2024 to 5 December 2024 (month 8)"),
        (MonthlyEpayeTaxPeriod(DecemberJanuary, TaxYear(2025)), Lang("en"), "6 December 2024 to 5 January 2025 (month 9)"),
        (MonthlyEpayeTaxPeriod(JanuaryFebruary, TaxYear(2025)), Lang("cy"), "6 Ionawr 2025 i 5 Chwefror 2025 (mis 10)"),
        (MonthlyEpayeTaxPeriod(FebruaryMarch, TaxYear(2025)), Lang("cy"), "6 Chwefror 2025 i 5 Mawrth 2025 (mis 11)"),
        (MonthlyEpayeTaxPeriod(MarchApril, TaxYear(2025)), Lang("cy"), "6 Mawrth 2025 i 5 Ebrill 2025 (mis 12)"),
        (MonthlyEpayeTaxPeriod(AprilMay, TaxYear(2025)), Lang("cy"), "6 Ebrill 2024 i 5 Mai 2024 (mis 1)"),
        (MonthlyEpayeTaxPeriod(MayJune, TaxYear(2025)), Lang("cy"), "6 Mai 2024 i 5 Mehefin 2024 (mis 2)"),
        (MonthlyEpayeTaxPeriod(JuneJuly, TaxYear(2025)), Lang("cy"), "6 Mehefin 2024 i 5 Gorffennaf 2024 (mis 3)"),
        (MonthlyEpayeTaxPeriod(JulyAugust, TaxYear(2025)), Lang("cy"), "6 Gorffennaf 2024 i 5 Awst 2024 (mis 4)"),
        (MonthlyEpayeTaxPeriod(AugustSeptember, TaxYear(2025)), Lang("cy"), "6 Awst 2024 i 5 Medi 2024 (mis 5)"),
        (MonthlyEpayeTaxPeriod(SeptemberOctober, TaxYear(2025)), Lang("cy"), "6 Medi 2024 i 5 Hydref 2024 (mis 6)"),
        (MonthlyEpayeTaxPeriod(OctoberNovember, TaxYear(2025)), Lang("cy"), "6 Hydref 2024 i 5 Tachwedd 2024 (mis 7)"),
        (MonthlyEpayeTaxPeriod(NovemberDecember, TaxYear(2025)), Lang("cy"), "6 Tachwedd 2024 i 5 Rhagfyr 2024 (mis 8)"),
        (MonthlyEpayeTaxPeriod(DecemberJanuary, TaxYear(2025)), Lang("cy"), "6 Rhagfyr 2024 i 5 Ionawr 2025 (mis 9)"),
        (QuarterlyEpayeTaxPeriod(AprilJuly, TaxYear(2025)), Lang("en"), "6 April 2024 to 5 July 2024 (first quarter)"),
        (QuarterlyEpayeTaxPeriod(AprilJuly, TaxYear(2025)), Lang("cy"), "6 Ebrill 2024 i 5 Gorffennaf 2024 (chwarter cyntaf)"),
        (QuarterlyEpayeTaxPeriod(JulyOctober, TaxYear(2025)), Lang("en"), "6 July 2024 to 5 October 2024 (second quarter)"),
        (QuarterlyEpayeTaxPeriod(JulyOctober, TaxYear(2025)), Lang("cy"), "6 Gorffennaf 2024 i 5 Hydref 2024 (ail chwarter)"),
        (QuarterlyEpayeTaxPeriod(OctoberJanuary, TaxYear(2025)), Lang("en"), "6 October 2024 to 5 January 2025 (third quarter)"),
        (QuarterlyEpayeTaxPeriod(OctoberJanuary, TaxYear(2025)), Lang("cy"), "6 Hydref 2024 i 5 Ionawr 2025 (trydydd chwarter)"),
        (QuarterlyEpayeTaxPeriod(JanuaryApril, TaxYear(2025)), Lang("en"), "6 January 2025 to 5 April 2025 (fourth quarter)"),
        (QuarterlyEpayeTaxPeriod(JanuaryApril, TaxYear(2025)), Lang("cy"), "6 Ionawr 2025 i 5 Ebrill 2025 (pedwerydd chwarter)")
      )

      forAll(testCases) { (period: FixedLengthEpayeTaxPeriod, lang: Lang, expectedResult: String) =>
        Period.humanReadablePeriod(period)(lang) shouldBe expectedResult
      }
    }
  }

  "displayCalendarQuarter" - {
    "should return a string indicating the CalendarQuarterlyPeriod" in {
      Period.displayCalendarQuarterAndYear(
        CalendarQuarterlyPeriod(CalendarQuarter.JanuaryToMarch, 2027)
      ) shouldBe "January to March 2027" withClue "JanuaryToMarch was wrong"
      Period.displayCalendarQuarterAndYear(
        CalendarQuarterlyPeriod(CalendarQuarter.AprilToJune, 2027)
      ) shouldBe "April to June 2027" withClue "AprilToJune was wrong"
      Period.displayCalendarQuarterAndYear(
        CalendarQuarterlyPeriod(CalendarQuarter.JulyToSeptember, 2027)
      ) shouldBe "July to September 2027" withClue "JulyToSeptember was wrong"
      Period.displayCalendarQuarterAndYear(
        CalendarQuarterlyPeriod(CalendarQuarter.OctoberToDecember, 2027)
      ) shouldBe "October to December 2027" withClue "OctoberToDecember was wrong"
    }
  }

  "displayCalendarPeriodMonth" - {
    "Period.displayCalendarPeriodMonth can handle January" in {
      Period.displayCalendarPeriodMonthAndYear(CalendarPeriod(1, 2027)) shouldBe "January 2027"
    }

    "Period.displayCalendarPeriodMonth can handle December" in {
      Period.displayCalendarPeriodMonthAndYear(CalendarPeriod(12, 2027)) shouldBe "December 2027"
    }

    "Period.displayCalendarPeriodMonth will not have a month for numbers equal or lower than zero" in {
      Period.displayCalendarPeriodMonthAndYear(CalendarPeriod(0, 2027)) shouldBe ""
      Period.displayCalendarPeriodMonthAndYear(CalendarPeriod(-98, 2027)) shouldBe ""
    }

    "Period.displayCalendarPeriodMonth will not have a month for numbers above 12" in {
      Period.displayCalendarPeriodMonthAndYear(CalendarPeriod(13, 2027)) shouldBe ""
    }
  }

}
