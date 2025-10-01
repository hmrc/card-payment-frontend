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
import payapi.corcommon.model.times.period.CalendarQuarter.{AprilToJune, JanuaryToMarch, JulyToSeptember, OctoberToDecember}
import payapi.corcommon.model.times.period.CalendarQuarterlyPeriod
import play.api.i18n.{Lang, Messages}

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

  def displayCalendarQuarter(period: CalendarQuarterlyPeriod): String = period.quarter match {
    case JanuaryToMarch    => "period.calendar-quarterly.january-to-march" + period.year
    case AprilToJune       => "period.calendar-quarterly.april-to-june" + period.year
    case JulyToSeptember   => "period.calendar-quarterly.july-to-september" + period.year
    case OctoberToDecember => "period.calendar-quarterly.october-to-december" + period.year
  }

  def displayCalendarPeriodMonth(period: CalendarPeriod): String = {
    period.month match {
      case 1  => "period.calendar-month.january" + period.year
      case 2  => "period.calendar-month.february" + period.year
      case 3  => "period.calendar-month.march" + period.year
      case 4  => "period.calendar-month.april" + period.year
      case 5  => "period.calendar-month.may" + period.year
      case 6  => "period.calendar-month.june" + period.year
      case 7  => "period.calendar-month.july" + period.year
      case 8  => "period.calendar-month.august" + period.year
      case 9  => "period.calendar-month.september" + period.year
      case 10 => "period.calendar-month.october" + period.year
      case 11 => "period.calendar-month.november" + period.year
      case 12 => "period.calendar-month.december" + period.year
      case _  => "NO MONTH THAT MATCHES"
    }
  }

}

