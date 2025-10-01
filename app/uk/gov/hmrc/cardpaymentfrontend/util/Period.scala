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

import payapi.corcommon.model.taxes.epaye.{FixedLengthEpayeTaxPeriod, MonthlyEpayeTaxPeriod, QuarterlyEpayeTaxPeriod, YearlyEpayeTaxPeriod}
import payapi.corcommon.model.taxes.vat.CalendarPeriod
import payapi.corcommon.model.times.period.CalendarQuarter
import payapi.corcommon.model.times.period.CalendarQuarter.{AprilToJune, JanuaryToMarch, JulyToSeptember, OctoberToDecember}
import play.api.i18n.Lang

object Period {

  def humanReadablePeriod(period: FixedLengthEpayeTaxPeriod)(implicit lang: Lang): String =
    if (lang.code.contains("cy")) {
      period.humanReadablePeriodInWelsh
    } else {
      period.humanReadablePeriodInEnglish
    }

  implicit class FixedLengthEpayeTaxPeriodExt(val period: FixedLengthEpayeTaxPeriod) extends AnyVal {

    def humanReadablePeriodInEnglish: String = period match {
      case p: YearlyEpayeTaxPeriod  => s"${p.taxYear.startYear.toString} to ${p.taxYear.endYear.toString}"
      case p: MonthlyEpayeTaxPeriod => s"${DateUtils.getDateRangeAsEnglish(p.taxFrom, p.taxTo)} (month ${p.taxMonth.intValue.toString})"
      case p: QuarterlyEpayeTaxPeriod =>
        val getQuarterEnglish: String = p.taxQuarter.intValue match {
          case 1 => " (first quarter)"
          case 2 => " (second quarter)"
          case 3 => " (third quarter)"
          case 4 => " (fourth quarter)"
        }
        DateUtils.getDateRangeAsEnglish(p.taxFrom, p.taxTo) + getQuarterEnglish
    }

    def humanReadablePeriodInWelsh: String = period match {
      case p: YearlyEpayeTaxPeriod  => s"${p.taxYear.startYear.toString} i ${p.taxYear.endYear.toString}"
      case p: MonthlyEpayeTaxPeriod => s"${DateUtils.getDateRangeAsWelsh(p.taxFrom, p.taxTo)} (mis ${p.taxMonth.intValue.toString})"
      case p: QuarterlyEpayeTaxPeriod =>
        val getQuarterWelsh: String = p.taxQuarter.intValue match {
          case 1 => " (chwarter cyntaf)"
          case 2 => " (ail chwarter)"
          case 3 => " (trydydd chwarter)"
          case 4 => " (pedwerydd chwarter)"
        }
        DateUtils.getDateRangeAsWelsh(p.taxFrom, p.taxTo) + getQuarterWelsh
    }

  }

  def displayCalendarQuarter(quarter: CalendarQuarter): String = quarter match {
    case JanuaryToMarch =>


      english = "January 1 to March 31",
      welsh   = "Ionawr 1 i Mawrth 31"
    case AprilToJune => Message(
      english = "April 1 to June 30",
      welsh   = "Ebrill 1 i Mehefin 30"
    )
    case JulyToSeptember => Message(
      english = "July 1 to September 30",
      welsh   = "Gorffenn 1 i Medi 30"
    )
    case OctoberToDecember => Message(
      english = "October 1 to December 31",
      welsh   = "Hydref 1 i Rhagfyr 31"
    )
  }

  def displayCalendarPeriodMonth(period: CalendarPeriod): String = {
    val month = period.month - 1
    val monthNames: Seq[String] = Seq(
      "January",
      "February",
      "March",
      "April",
      "May",
      "June",
      "July",
      "August",
      "September",
      "October",
      "November",
      "December"
    )
    if (period.month >= 1 && period.month <= 12) monthNames(month) else ""
  }

}

