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

package uk.gov.hmrc.cardpaymentfrontend.views

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.cardpaymentfrontend.controllers.{AddressController, FeesController, PaymentCompleteController}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.testsupport.{ItSpec, TestHelpers}

import scala.jdk.CollectionConverters.CollectionHasAsScala

class LayoutSpec extends ItSpec {

  private val feesController: FeesController       = app.injector.instanceOf[FeesController]
  private val addressController: AddressController = app.injector.instanceOf[AddressController]
  private val paymentCompleteController            = app.injector.instanceOf[PaymentCompleteController]

  "HMRC Standard Header is shown correctly" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    val result   = feesController.renderPage(fakeRequest)
    val document = Jsoup.parse(contentAsString(result))

    val govUkLogoLink = document.select(".govuk-header__logo a").first()
    govUkLogoLink.attr("href") shouldBe "https://www.gov.uk"
    govUkLogoLink.text().trim shouldBe "GOV.UK"

    val serviceNameLink = document.select(".govuk-header__service-name").first()
    serviceNameLink.text().trim shouldBe "Pay your Self Assessment"
  }

  "HMRC Standard Footer is shown correctly" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    val result   = feesController.renderPage(fakeRequest)
    val document = Jsoup.parse(contentAsString(result))

    val footerLinks = document.select(".govuk-footer__link").asScala
    footerLinks.find(_.text().trim == "Privacy policy").map(_.attr("href")) shouldBe Some("/help/privacy")
    footerLinks.find(_.text().trim == "Terms and conditions").map(_.attr("href")) shouldBe Some("/help/terms-and-conditions")
    footerLinks.find(_.text().trim == "Help using GOV.UK").map(_.attr("href")) shouldBe Some("https://www.gov.uk/help")
    footerLinks.find(_.text().trim == "Contact").map(_.attr("href")) shouldBe Some("https://www.gov.uk/government/organisations/hm-revenue-customs/contact")
    footerLinks.find(_.text().trim == "© Crown copyright").map(_.attr("href")) shouldBe Some(
      "https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/"
    )
  }

  "Accessibility Statement Link is correct" in {

    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    val result            = feesController.renderPage(fakeRequest)
    val document          = Jsoup.parse(contentAsString(result))
    val accessibilityItem =
      document.select("ul.govuk-footer__inline-list li.govuk-footer__inline-list-item a").asScala.find(_.text().trim == "Accessibility statement")
    accessibilityItem.flatMap(item => Option(item.attr("href"))) shouldBe Some("http://localhost:12346/accessibility-statement/pay?referrerUrl=%2Fcard-fees")
  }

  "render page with a back link" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    val result   = feesController.renderPage(fakeRequest)
    val document = Jsoup.parse(contentAsString(result))

    val backLinkElement = document.select(".govuk-back-link").first()
    backLinkElement.text().trim shouldBe "Back"
    backLinkElement.attr("href") shouldBe "#"
  }

  "render page with a back link in welsh" in {
    val fakeRequestInWelsh = FakeRequest("GET", "/card-fees").withSessionId().withLangWelsh()

    val result   = feesController.renderPage(fakeRequestInWelsh)
    val document = Jsoup.parse(contentAsString(result))

    val backLink = document.select(".govuk-back-link")
    backLink.text() shouldBe "Yn ôl"
    backLink.attr("href") shouldBe "#"
  }

  "HMRC Technical Issue Helper is present" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    val result   = feesController.renderPage(fakeRequest)
    val document = Jsoup.parse(contentAsString(result))

    val techIssueLink = document.select("a.hmrc-report-technical-issue").first()
    techIssueLink.text().trim shouldBe "Is this page not working properly? (opens in new tab)"
    techIssueLink.attr("href") should include("/contact/report-technical-problem")
  }

  "render layout with /pay/make-a-payment-now as the service url href when origin is a webchat origin" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()
    TestHelpers.webChatOrigins.diff(TestHelpers.unimplementedOrigins).foreach { o =>
      PayApiStub.stubForFindBySessionId2xx(TestHelpers.deriveTestDataFromOrigin(o).journeyBeforeBeginWebPayment)
      val result   = addressController.renderPage(fakeRequest)
      val document = Jsoup.parse(contentAsString(result))
      document
        .select(".govuk-header__content")
        .select("a")
        .attr(
          "href"
        ) shouldBe "http://localhost:9056/pay/make-a-payment-now" withClue s"expected href to be webchat landing page for implemented webchat origin: ${o.entryName}"
    }
  }

  "render payment-complete page as the service url href when origin is a webchat origin and payment is completed" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()
    TestHelpers.webChatOrigins.diff(TestHelpers.unimplementedOrigins).foreach { o =>
      PayApiStub.stubForFindBySessionId2xx(TestHelpers.deriveTestDataFromOrigin(o).journeyAfterSucceedDebitWebPayment)
      val result   = paymentCompleteController.renderPage(fakeRequest)
      val document = Jsoup.parse(contentAsString(result))
      document
        .select(".govuk-header__content")
        .select("a")
        .attr(
          "href"
        ) shouldBe "/pay-by-card/payment-complete" withClue s"expected href to be payment-complete page for implemented webchat origin: ${o.entryName}"
    }
  }

  "render layout with /pay as the service url href when origin is not a webchat origin" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()
    TestHelpers.implementedOrigins.diff(TestHelpers.webChatOrigins).foreach { o =>
      PayApiStub.stubForFindBySessionId2xx(TestHelpers.deriveTestDataFromOrigin(o).journeyBeforeBeginWebPayment)
      val result   = addressController.renderPage(fakeRequest)
      val document = Jsoup.parse(contentAsString(result))
      document
        .select(".govuk-header__content")
        .select("a")
        .attr("href") shouldBe "http://localhost:9056/pay" withClue s"expected href to be /pay for origin: ${o.entryName}"
    }
  }
}
