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

import play.api.i18n.MessagesApi
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.*
import java.time.{LocalDateTime, Month}

class DateStringBuilderSpec extends ItSpec {

  def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]

  Seq(
    ("January", LocalDateTime.of(2024, Month.JANUARY, 1, 1, 1, 1), "1 January 2024", "1 Ionawr 2024"),
    ("February", LocalDateTime.of(2024, Month.FEBRUARY, 1, 1, 1, 1), "1 February 2024", "1 Chwefror 2024"),
    ("March", LocalDateTime.of(2024, Month.MARCH, 1, 1, 1, 1), "1 March 2024", "1 Mawrth 2024"),
    ("April", LocalDateTime.of(2024, Month.APRIL, 1, 1, 1, 1), "1 April 2024", "1 Ebrill 2024"),
    ("May", LocalDateTime.of(2024, Month.MAY, 1, 1, 1, 1), "1 May 2024", "1 Mai 2024"),
    ("June", LocalDateTime.of(2024, Month.JUNE, 1, 1, 1, 1), "1 June 2024", "1 Mehefin 2024"),
    ("July", LocalDateTime.of(2024, Month.JULY, 1, 1, 1, 1), "1 July 2024", "1 Gorffenn 2024"),
    ("August", LocalDateTime.of(2024, Month.AUGUST, 1, 1, 1, 1), "1 August 2024", "1 Awst 2024"),
    ("September", LocalDateTime.of(2024, Month.SEPTEMBER, 1, 1, 1, 1), "1 September 2024", "1 Medi 2024"),
    ("October", LocalDateTime.of(2024, Month.OCTOBER, 1, 1, 1, 1), "1 October 2024", "1 Hydref 2024"),
    ("November", LocalDateTime.of(2024, Month.NOVEMBER, 1, 1, 1, 1), "1 November 2024", "1 Tachwedd 2024"),
    ("December", LocalDateTime.of(2024, Month.DECEMBER, 1, 1, 1, 1), "1 December 2024", "1 Rhagfyr 2024")
  ).foreach { case (month, localDate, expectedEnglish, expectedWelsh) =>
    s"DateStringBuilder.getDateAsString should return the correct english message for $month" in {
      val fakeRequest = FakeRequest("GET", "/blah")
      DateStringBuilder.getDateAsString(localDate)(messagesApi.preferred(fakeRequest)) shouldBe expectedEnglish withClue s"Wrong date string for $month"
    }

    s"DateStringBuilder.getDateAsString should return the correct welsh message for $month" in {
      val fakeRequest = FakeRequest("GET", "/blah").withLangWelsh()
      DateStringBuilder.getDateAsString(localDate)(messagesApi.preferred(fakeRequest)) shouldBe expectedWelsh withClue s"Wrong date string for $month"
    }

  }

}
