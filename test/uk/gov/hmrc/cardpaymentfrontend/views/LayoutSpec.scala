package uk.gov.hmrc.cardpaymentfrontend.views

import org.jsoup.Jsoup
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import uk.gov.hmrc.cardpaymentfrontend.controllers.FeesController
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

import scala.jdk.CollectionConverters.CollectionHasAsScala

class LayoutSpec extends ItSpec {

  private val feesController: FeesController = app.injector.instanceOf[FeesController]

  "HMRC Standard Header is shown correctly" in {
    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    val result = feesController.renderPage(fakeRequest)
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
    val result = feesController.renderPage(fakeRequest)
    val document = Jsoup.parse(contentAsString(result))

    val footerLinks = document.select(".govuk-footer__link").asScala
    footerLinks.find(_.text().trim == "Privacy policy").map(_.attr("href")) shouldBe Some("/help/privacy")
    footerLinks.find(_.text().trim == "Terms and conditions").map(_.attr("href")) shouldBe Some("/help/terms-and-conditions")
    footerLinks.find(_.text().trim == "Help using GOV.UK").map(_.attr("href")) shouldBe Some("https://www.gov.uk/help")
    footerLinks.find(_.text().trim == "Contact").map(_.attr("href")) shouldBe Some("https://www.gov.uk/government/organisations/hm-revenue-customs/contact")
    footerLinks.find(_.text().trim == "© Crown copyright").map(_.attr("href")) shouldBe Some("https://www.nationalarchives.gov.uk/information-management/re-using-public-sector-information/uk-government-licensing-framework/crown-copyright/")
  }

  "Accessibility Statement Link is correct" in {

    val fakeRequest = FakeRequest("GET", "/card-fees").withSessionId()

    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    val result = feesController.renderPage(fakeRequest)
    val document = Jsoup.parse(contentAsString(result))
    val accessibilityItem = document.select("ul.govuk-footer__inline-list li.govuk-footer__inline-list-item a").asScala.find(_.text().trim == "Accessibility statement")
    accessibilityItem.flatMap(item => Option(item.attr("href"))) shouldBe Some("http://localhost:12346/accessibility-statement/pay?referrerUrl=%2Fcard-fees")
  }

}
