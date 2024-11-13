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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.corcommon.model.JourneyId
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{AnyContent, Call}
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, CheckYourAnswersRow, EmailAddress, Link}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps._

class ExtendedPfSaSpec extends ItSpec {
  val fakeGetRequest = FakeRequest("GET", "/cya0").withSessionId()
  def messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
  implicit val messages: Messages = messagesApi.preferred(fakeGetRequest)
  val testJourney = TestJourneys.PfSa.testPfSaJourneySuccessDebit
  val systemUnderTest = ExtendedPfSa

  "ExtendedBtaSa CYA" - {
    "there should be four rows" in {
      PayApiStub.stubForFindBySessionId2xx(testJourney)
      val address = Address(line1    = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = "AA1AA", country = "UK")
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourney, fakeGetRequest.withAddress(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"), address))

      val rows: Seq[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersRows(fakeJourneyRequest)
      rows.size shouldBe 4
    }

    "contains a reference row with the right title and value" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)

      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit, fakeGetRequest)
      val rows: Seq[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersRows(fakeJourneyRequest)
      val referenceRow: CheckYourAnswersRow = rows.headOption.getOrElse(CheckYourAnswersRow("", Seq.empty, None))
      referenceRow.titleMessageKey shouldBe "pfsa.reference.title"
      referenceRow.value shouldBe Seq("1234567895")
      referenceRow.changeLink.isDefined shouldBe true
    }

    "contains an amount row with the right title and value" in {
      PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)

      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit, fakeGetRequest)
      val rows: Seq[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersRows(fakeJourneyRequest)
      val amountRow: CheckYourAnswersRow = rows.lift(1).getOrElse(CheckYourAnswersRow("", Seq.empty, None))
      amountRow.titleMessageKey shouldBe "pfsa.amount.title"
      amountRow.value shouldBe Seq("£12.34")
    }

    "with a full address, create a sequence of string that hold the address" in {
      PayApiStub.stubForFindBySessionId2xx(testJourney)
      val address = Address(line1    = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = "AA1AA", country = "UK")
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourney, fakeGetRequest.withAddress(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"), address))

      val rows: Seq[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersRows(fakeJourneyRequest)
      val addressRow: CheckYourAnswersRow = rows.lift(2).getOrElse(CheckYourAnswersRow("", Seq.empty, None))
      addressRow.titleMessageKey shouldBe "pfsa.address.title"
      addressRow.value shouldBe Seq("line1", "line2", "city", "county", "AA1AA", "UK")
    }

    "if there is an email address in the session contains the email address" in {
      PayApiStub.stubForFindBySessionId2xx(testJourney)
      val email = EmailAddress("this@that.com")
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourney, fakeGetRequest.withEmailInSession(JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"), email))

      val rows: Seq[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersRows(fakeJourneyRequest)
      val emailRow: CheckYourAnswersRow = rows.lift(3).getOrElse(CheckYourAnswersRow("", Seq.empty, None))
      emailRow.titleMessageKey shouldBe "pfsa.email.title"
      emailRow.value shouldBe Seq("this@that.com")
      emailRow.changeLink.getOrElse(Link(Call("", ""), "", "")).messageKey shouldBe "pfsa.email.supply-link.text.change"
    }

    "if there is no email address in the session contains the email address" in {
      PayApiStub.stubForFindBySessionId2xx(testJourney)
      val fakeJourneyRequest: JourneyRequest[AnyContent] = new JourneyRequest(testJourney, fakeGetRequest)

      val rows: Seq[CheckYourAnswersRow] = systemUnderTest.checkYourAnswersRows(fakeJourneyRequest)
      val emailRow: CheckYourAnswersRow = rows.lift(3).getOrElse(CheckYourAnswersRow("", Seq.empty, None))
      emailRow.titleMessageKey shouldBe "pfsa.email.title"
      emailRow.value shouldBe Seq.empty
      emailRow.changeLink.getOrElse(Link(Call("", ""), "", "")).messageKey shouldBe "pfsa.email.supply-link.text.new"
    }
  }
}
