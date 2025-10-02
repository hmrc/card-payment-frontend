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

  // Message keys not needed as no Welsh required for these particular origins - NiEuVat
  def displayCalendarQuarterAndYear(period: CalendarQuarterlyPeriod): String = period.quarter match {
    case JanuaryToMarch    => "January to March " + period.year.toString
    case AprilToJune       => "April to June " + period.year.toString
    case JulyToSeptember   => "July to September " + period.year.toString
    case OctoberToDecember => "October to December " + period.year.toString
  }

  // Message keys not needed as no Welsh required for these particular origins - NiEuVat
  def displayCalendarPeriodMonthAndYear(period: CalendarPeriod): String = {
    val yearStr = period.year.toString
    period.month match {
      case 1  => "January " + yearStr
      case 2  => "February " + yearStr
      case 3  => "March " + yearStr
      case 4  => "April " + yearStr
      case 5  => "May " + yearStr
      case 6  => "June " + yearStr
      case 7  => "July " + yearStr
      case 8  => "August " + yearStr
      case 9  => "September " + yearStr
      case 10 => "October " + yearStr
      case 11 => "November " + yearStr
      case 12 => "December " + yearStr
      case _  => ""
    }
  }

}

