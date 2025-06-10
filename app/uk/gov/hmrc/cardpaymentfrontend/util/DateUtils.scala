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

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
  private val MonthNamesInWelsh = Map(
    1 -> "Ionawr",
    2 -> "Chwefror",
    3 -> "Mawrth",
    4 -> "Ebrill",
    5 -> "Mai",
    6 -> "Mehefin",
    7 -> "Gorffennaf",
    8 -> "Awst",
    9 -> "Medi",
    10 -> "Hydref",
    11 -> "Tachwedd",
    12 -> "Rhagfyr"
  )

  def getDateRangeAsWelsh(startDate: LocalDate, endDate: LocalDate): String = s"${getDateAsWelsh(startDate)} i ${getDateAsWelsh(endDate)}"
  private def getDateAsWelsh(date: LocalDate): String =
    s"${date.getDayOfMonth.toString} ${MonthNamesInWelsh(date.getMonthValue)} ${date.getYear.toString}"

  def getDateRangeAsEnglish(startDate: LocalDate, endDate: LocalDate): String = s"${getDateAsEnglish(startDate)} to ${getDateAsEnglish(endDate)}"
  private def getDateAsEnglish(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("d MMMM yyyy"))

}
