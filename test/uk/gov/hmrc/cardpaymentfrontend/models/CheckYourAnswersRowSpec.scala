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

import play.api.mvc.{AnyContentAsEmpty, Call, Cookie}
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import play.api.i18n._
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.{Empty, HtmlContent, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow, Value}

class CheckYourAnswersRowSpec extends ItSpec {
  def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/cya0")
  val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/email-address").withCookies(Cookie("PLAY_LANG", "cy"))

  "summarise" - {
    "will generate a minimum SummaryListRow from a minimum CheckYourAnswersRow" in {
      implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)

      val checkYourAnswersRow: CheckYourAnswersRow = CheckYourAnswersRow("", Seq.empty, None)
      val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
      result shouldBe SummaryListRow(Key(Text("")), Value(Empty))
    }

    "will generate a minimum SummaryListRow with a key from English messages if the key is set in CheckYourAnswersRow" in {
      implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)

      val checkYourAnswersRow: CheckYourAnswersRow = CheckYourAnswersRow("check-your-answers.PfSa.reference", Seq.empty, None)
      val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
      result shouldBe SummaryListRow(Key(Text("Unique Taxpayer Reference (UTR)")), Value(Empty))
    }

    "will generate a minimum SummaryListRow with a key from Welsh messages if the key is set in CheckYourAnswersRow" in {
      implicit val messages: Messages = messagesApi.preferred(fakeGetRequestInWelsh)

      val checkYourAnswersRow: CheckYourAnswersRow = CheckYourAnswersRow("check-your-answers.PfSa.reference", Seq.empty, None)
      val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
      result shouldBe SummaryListRow(Key(Text("Cyfeirnod Unigryw y Trethdalwr (UTR)")), Value(Empty))
    }

    "will generate a SummaryListRow with a value if the value is set in CheckYourAnswersRow" in {
      implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)

      val checkYourAnswersRow: CheckYourAnswersRow = CheckYourAnswersRow("check-your-answers.PfSa.reference", Seq("XARefExample"), None)
      val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
      result shouldBe SummaryListRow(Key(Text("Unique Taxpayer Reference (UTR)")), Value(HtmlContent("XARefExample")))
    }

    "will generate a SummaryListRow with a separated lines if the value is set as multiple strings in CheckYourAnswersRow" in {
      implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)

      val checkYourAnswersRow: CheckYourAnswersRow = CheckYourAnswersRow("check-your-answers.PfSa.reference", Seq("Line1", "Line2"), None)
      val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
      result shouldBe SummaryListRow(Key(Text("Unique Taxpayer Reference (UTR)")), Value(HtmlContent("Line1</br>Line2")))
    }

    "will generate a SummaryListRow with a link if the link is set in CheckYourAnswersRow" in {
      implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)

      val checkYourAnswersRow: CheckYourAnswersRow =
        CheckYourAnswersRow(
          "check-your-answers.PfSa.reference",
          Seq("XARefExample"),
          Some(Link(Call("GET", "some-href"), "linkId", "check-your-answers.change"))
        )
      val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
      result shouldBe
        SummaryListRow(
          Key(Text("Unique Taxpayer Reference (UTR)")),
          Value(HtmlContent("XARefExample")),
          actions = Some(Actions(items = Seq(ActionItem(
            "some-href",
            Text("Change"),
            attributes = Map("id" -> "linkId")
          ))))
        )
    }
  }

  "will generate a  SummaryListRow with html content following the Missing Information UI pattern if the value is missing in CheckYourAnswersRow" in {
    implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)

    val checkYourAnswersRow: CheckYourAnswersRow =
      CheckYourAnswersRow(
        "check-your-answers.PfSa.reference",
        Seq.empty,
        Some(Link(Call("GET", "some-href"), "linkId", "check-your-answers.change"))
      )
    val result = CheckYourAnswersRow.summarise(checkYourAnswersRow)
    result shouldBe
      SummaryListRow(
        Key(Text("Unique Taxpayer Reference (UTR)")),
        Value(HtmlContent("""<a id="linkId" href="some-href" class="govuk-link">Change</a>"""))
      )
  }
}
