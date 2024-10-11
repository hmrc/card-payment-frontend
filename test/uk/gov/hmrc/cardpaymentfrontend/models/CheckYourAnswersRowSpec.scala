/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import play.api.i18n._
import play.api.test.Helpers._

class CheckYourAnswersRowSpec extends ItSpec {
  //private val systemUnderTest: CheckYourAnswersRow = app.injector.instanceOf[CheckYourAnswersRow]

  "summarise" - {
    val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/cya0")
    implicit val m: Messages = app.injector.instanceOf[Messages]

    val checkYourAnswersRow: CheckYourAnswersRow = CheckYourAnswersRow("", None, None)
    val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
    println(s"====== result: ${result.toString}")
  }
}
