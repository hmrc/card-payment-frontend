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

import play.api.i18n.Messages

import java.time.{LocalDateTime, Month}

object DateStringBuilder {

  def getDateAsString(localDate: LocalDateTime)(implicit messages: Messages): String = {
    val day  = localDate.getDayOfMonth
    val year = localDate.getYear.toString
    localDate.getMonth match {
      case Month.JANUARY   => Messages("date.day-month-year.january", day, year)
      case Month.FEBRUARY  => Messages("date.day-month-year.february", day, year)
      case Month.MARCH     => Messages("date.day-month-year.march", day, year)
      case Month.APRIL     => Messages("date.day-month-year.april", day, year)
      case Month.MAY       => Messages("date.day-month-year.may", day, year)
      case Month.JUNE      => Messages("date.day-month-year.june", day, year)
      case Month.JULY      => Messages("date.day-month-year.july", day, year)
      case Month.AUGUST    => Messages("date.day-month-year.august", day, year)
      case Month.SEPTEMBER => Messages("date.day-month-year.september", day, year)
      case Month.OCTOBER   => Messages("date.day-month-year.october", day, year)
      case Month.NOVEMBER  => Messages("date.day-month-year.november", day, year)
      case Month.DECEMBER  => Messages("date.day-month-year.december", day, year)
    }
  }

}
