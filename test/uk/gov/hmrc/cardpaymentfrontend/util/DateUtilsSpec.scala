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

import org.scalatest.prop.TableDrivenPropertyChecks
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

import java.time.LocalDate

class DateUtilsSpec extends UnitSpec with TableDrivenPropertyChecks {

  "Methods tests" - {
    "getDateRangeAsWelsh" in {
      val testCases = Table(
        ("startDate", "endDate", "expectedResult"),
        (LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31), "1 Ionawr 2023 i 31 Ionawr 2023"),
        (LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 28), "1 Chwefror 2023 i 28 Chwefror 2023"),
        (LocalDate.of(2023, 12, 1), LocalDate.of(2023, 12, 31), "1 Rhagfyr 2023 i 31 Rhagfyr 2023")
      )

      forAll(testCases) { (startDate, endDate, expectedResult) =>
        DateUtils.getDateRangeAsWelsh(startDate, endDate) shouldBe expectedResult
      }
    }

    "getDateRangeAsEnglish" in {
      val testCases = Table(
        ("startDate", "endDate", "expectedResult"),
        (LocalDate.of(2023, 1, 1), LocalDate.of(2023, 1, 31), "1 January 2023 to 31 January 2023"),
        (LocalDate.of(2023, 2, 1), LocalDate.of(2023, 2, 28), "1 February 2023 to 28 February 2023"),
        (LocalDate.of(2023, 12, 1), LocalDate.of(2023, 12, 31), "1 December 2023 to 31 December 2023")
      )

      forAll(testCases) { (startDate, endDate, expectedResult) =>
        DateUtils.getDateRangeAsEnglish(startDate, endDate) shouldBe expectedResult
      }
    }
  }
}
