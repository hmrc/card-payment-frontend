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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import payapi.corcommon.model.Origins
import play.api.http.Status
import play.api.mvc.{AnyContentAsEmpty, Call, Cookie}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import play.api.test.Helpers._
import uk.gov.hmrc.cardpaymentfrontend.models.Link
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethods

import scala.jdk.CollectionConverters.CollectionHasAsScala

class FeesControllerSpec extends ItSpec {
  private val systemUnderTest: FeesController = app.injector.instanceOf[FeesController]

  "FeesController" - {
    "GET /card-fees" - {
      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees0")
      val fakeGetRequestInWelsh: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/email-address").withCookies(Cookie("PLAY_LANG", "cy"))

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "render the page with the hmrc layout" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("html").hasClass("govuk-template") shouldBe true withClue "no govuk template"
      }

      "render the page with the h1 correctly in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("h1").text() shouldBe "Card fees" withClue "Card fees page H1 wrong"
      }

      "render the page with the h1 correctly in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select("h1").text() shouldBe "Ffioedd cerdyn" withClue "Card fees Welsh page H1 wrong"
      }

      "render the page with the language toggle" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
        langToggleText should contain theSameElementsAs List("English", "Newid yr iaith ir Gymraeg Cymraeg") //checking the visually hidden text, it's simpler
      }

      "render the page with a back button" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val backButton = document.select(".govuk-back-link")
        backButton.text() shouldBe "Back"
        backButton.attr("href") shouldBe "#"
      }

      "render the page with a back button in welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val backButton = document.select(".govuk-back-link")
        backButton.text() shouldBe "Yn ôl"
        backButton.attr("href") shouldBe "#"
      }

      "render card fee para 1 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para1 = document.select("#para1")
        para1.text() shouldBe "There is a non-refundable fee if you pay by corporate credit card or corporate debit card."
      }

      "render card fee para 2 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para2 = document.select("#para2")
        para2.text() shouldBe "There is no fee if you pay by:"
      }

      "render card fee para 3 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para3 = document.select("#para3")
        para3.text() shouldBe "You cannot pay using a personal credit card."
      }

      "render card fee para 4 in English" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val para4 = document.select("#para4")
        para4.text() shouldBe "Allow 3 working days for your payment to reach HMRC’s bank account."
      }

      "render card fee para 1 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para1 = document.select("#para1")
        para1.text() shouldBe "Bydd ffi na ellir ei had-dalu yn cael ei chodi os talwch â cherdyn credyd corfforaethol neu gerdyn debyd corfforaethol."
      }

      "render card fee para 2 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para2 = document.select("#para2")
        para2.text() shouldBe "Nid oes ffi yn cael ei chodi os talwch drwy un o’r dulliau canlynol:"
      }

      "render card fee para 3 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para3 = document.select("#para3")
        para3.text() shouldBe "Ni allwch dalu â cherdyn credyd personol."
      }

      "render card fee para 4 in Welsh" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        val para4 = document.select("#para4")
        para4.text() shouldBe "Dylech ganiatáu 3 diwrnod gwaith i’ch taliad gyrraedd cyfrif banc CThEM."
      }

      "when open banking is not allowed (example render0) there is no open banking content" in {
        val result = systemUnderTest.renderPage0()(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        val bankAccountLink = document.select("#open-banking-link")
        bankAccountLink.isEmpty shouldBe true
      }

      "when open banking is allowed (example render1) there is open-banking content" in {
        val fakeGetRequest1: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees1")
        val result = systemUnderTest.renderPage1()(fakeGetRequest1)
        val document = Jsoup.parse(contentAsString(result))
        val bankAccountLink = document.select("#open-banking-link")
        bankAccountLink.text() shouldBe "bank account"
      }

      "when variable direct debit is allowed and one off direct debit is not allowed (example render2) there is variable direct debit content" in {
        val fakeGetRequest2: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees2")
        val result = systemUnderTest.renderPage2()(fakeGetRequest2)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#direct-debit-link")
        variableDirectDebitLink.text() shouldBe "direct debit"
      }

      "when variable direct debit is not allowed and one off direct debit is allowed (example render3) there is variable direct debit content" in {
        val fakeGetRequest3: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees3")
        val result = systemUnderTest.renderPage3()(fakeGetRequest3)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#direct-debit-link")
        variableDirectDebitLink.text() shouldBe "direct debit"
      }

      "when both variable direct debit is allowed and one off direct debit is allowed (example render4) and there is a primary link there is variable direct debit content" in {
        val fakeGetRequest4: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees4")
        val result = systemUnderTest.renderPage4()(fakeGetRequest4)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#direct-debit-link-both-primary")
        variableDirectDebitLink.text() shouldBe "direct debit"
      }

      "when both variable direct debit is allowed and one off direct debit is allowed (example render5) and there is a secondary link there is variable direct debit content" in {
        val fakeGetRequest5: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/card-fees5")
        val result = systemUnderTest.renderPage5()(fakeGetRequest5)
        val document = Jsoup.parse(contentAsString(result))
        val variableDirectDebitLink = document.select("#direct-debit-link-both-secondary")
        variableDirectDebitLink.text() shouldBe "direct debit"
      }

      val fakeRequest = FakeRequest().withSessionId()
      val fakeWelshRequest = FakeRequest().withSessionId().withLangWelsh()

        def testStaticContentEnglish(document: Document): Assertion = {
          document.select("h1").text() shouldBe "Card fees"
          val para1 = document.select("#para1")
          para1.text() shouldBe "There is a non-refundable fee if you pay by corporate credit card or corporate debit card."
          val para2 = document.select("#para2")
          para2.text() shouldBe "There is no fee if you pay by:"
          val para3 = document.select("#para3")
          para3.text() shouldBe "You cannot pay using a personal credit card."
          val para4 = document.select("#para4")
          para4.text() shouldBe "Allow 3 working days for your payment to reach HMRC’s bank account."
        }

        def testStaticContentWelsh(document: Document): Assertion = {
          document.select("h1").text() shouldBe "Ffioedd cerdyn"
          val para1 = document.select("#para1")
          para1.text() shouldBe "Bydd ffi na ellir ei had-dalu yn cael ei chodi os talwch â cherdyn credyd corfforaethol neu gerdyn debyd corfforaethol."
          val para2 = document.select("#para2")
          para2.text() shouldBe "Nid oes ffi yn cael ei chodi os talwch drwy un o’r dulliau canlynol:"
          val para3 = document.select("#para3")
          para3.text() shouldBe "Ni allwch dalu â cherdyn credyd personol."
          val para4 = document.select("#para4")
          para4.text() shouldBe "Dylech ganiatáu 3 diwrnod gwaith i’ch taliad gyrraedd cyfrif banc CThEM."
        }

      "for origin PfSa" - {

        "render the static content correctly" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentEnglish(document)
        }

        "the static content correctly in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentWelsh(document)
        }

        "render three options for other ways to pay" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          listOfMethods.size() shouldBe 3
        }

        "render an option for open banking" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#open-banking-link")
          openBankingBullet.text() shouldBe "bank account"
          openBankingBullet.attr("href") shouldBe "/pay-by-card/start-open-banking"
        }

        "render an option for open banking in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#open-banking-link")
          openBankingBullet.text() shouldBe "cyfrif banc"
          openBankingBullet.attr("href") shouldBe "/pay-by-card/start-open-banking"
        }

        "render an option for one off direct debit" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val oneOffDirectDebitBullet = listOfMethods.select("#one-off-direct-debit-link")
          oneOffDirectDebitBullet.text() shouldBe "Direct Debit (one-off payment)"
          oneOffDirectDebitBullet.attr("href") shouldBe "http://localhost:9056/pay/pay-by-one-off-direct-debit"
        }

        "render an option for one off direct debit in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val oneOffDirectDebitBullet = listOfMethods.select("#one-off-direct-debit-link")
          oneOffDirectDebitBullet.text() shouldBe "Debyd Uniongyrchol (taliad untro)"
          oneOffDirectDebitBullet.attr("href") shouldBe "http://localhost:9056/pay/pay-by-one-off-direct-debit"
        }

        "render an option for personal debit card" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "personal debit card"
        }

        "render an option for personal debit card in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "cerdyn debyd personol"
        }
      }

      "for origin BtaSa" - {

        "render the static content correctly" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentEnglish(document)
        }

        "the static content correctly in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentWelsh(document)
        }

        "render three options for other ways to pay" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          listOfMethods.size() shouldBe 3
        }

        "render an option for open banking" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#open-banking-link")
          openBankingBullet.text() shouldBe "bank account"
          openBankingBullet.attr("href") shouldBe "/pay-by-card/start-open-banking"
        }

        "render an option for open banking in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#open-banking-link")
          openBankingBullet.text() shouldBe "cyfrif banc"
          openBankingBullet.attr("href") shouldBe "/pay-by-card/start-open-banking"
        }

        "render an option for one off direct debit" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val oneOffDirectDebitBullet = listOfMethods.select("#one-off-direct-debit-link")
          oneOffDirectDebitBullet.text() shouldBe "Direct Debit (one-off payment)"
          oneOffDirectDebitBullet.attr("href") shouldBe "http://localhost:9056/pay/pay-by-one-off-direct-debit"
        }

        "render an option for one off direct debit in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val oneOffDirectDebitBullet = listOfMethods.select("#one-off-direct-debit-link")
          oneOffDirectDebitBullet.text() shouldBe "Debyd Uniongyrchol (taliad untro)"
          oneOffDirectDebitBullet.attr("href") shouldBe "http://localhost:9056/pay/pay-by-one-off-direct-debit"
        }

        "render an option for personal debit card" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "personal debit card"
        }

        "render an option for personal debit card in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.BtaSa.testBtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "cerdyn debyd personol"
        }
      }

      "for origin PtaSa" - {

        "render the static content correctly" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentEnglish(document)
        }

        "the static content correctly in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentWelsh(document)
        }

        "render three options for other ways to pay" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          listOfMethods.size() shouldBe 3
        }

        "render an option for open banking" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#open-banking-link")
          openBankingBullet.text() shouldBe "bank account"
          openBankingBullet.attr("href") shouldBe "/pay-by-card/start-open-banking"
        }

        "render an option for open banking in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#open-banking-link")
          openBankingBullet.text() shouldBe "cyfrif banc"
          openBankingBullet.attr("href") shouldBe "/pay-by-card/start-open-banking"
        }

        "render an option for one off direct debit" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val oneOffDirectDebitBullet = listOfMethods.select("#one-off-direct-debit-link")
          oneOffDirectDebitBullet.text() shouldBe "Direct Debit (one-off payment)"
          oneOffDirectDebitBullet.attr("href") shouldBe "http://localhost:9056/pay/pay-by-one-off-direct-debit"
        }

        "render an option for one off direct debit in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val oneOffDirectDebitBullet = listOfMethods.select("#one-off-direct-debit-link")
          oneOffDirectDebitBullet.text() shouldBe "Debyd Uniongyrchol (taliad untro)"
          oneOffDirectDebitBullet.attr("href") shouldBe "http://localhost:9056/pay/pay-by-one-off-direct-debit"
        }

        "render an option for personal debit card" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "personal debit card"
        }

        "render an option for personal debit card in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.PtaSa.testPtaSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "cerdyn debyd personol"
        }
      }

      "for origin ItSa" - {

        "render the static content correctly" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentEnglish(document)
        }

        "the static content correctly in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          testStaticContentWelsh(document)
        }

        "render two options for other ways to pay" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          listOfMethods.size() shouldBe 2
        }

        "render an option for bank transfer" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#bank-transfer-link")
          openBankingBullet.text() shouldBe "bank transfer"
          openBankingBullet.attr("href") shouldBe "http://localhost:9056/pay/bac"
        }

        "render an option for bank transfer in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val openBankingBullet = listOfMethods.select("#bank-transfer-link")
          openBankingBullet.text() shouldBe "drosglwyddiad banc"
          openBankingBullet.attr("href") shouldBe "http://localhost:9056/pay/bac"
        }

        "render an option for personal debit card" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "personal debit card"
        }

        "render an option for personal debit card in welsh" in {
          PayApiStub.stubForFindBySessionId2xx(TestJourneys.ItSa.testItSaJourneySuccessDebit)
          val result = systemUnderTest.renderPageNew()(fakeWelshRequest)
          val document = Jsoup.parse(contentAsString(result))
          val listOfMethods = document.select("#payment-type-list").select("li")
          val cardBullet = listOfMethods.select("#personal-debit-card")
          cardBullet.text() shouldBe "cerdyn debyd personol"
        }
      }
    }

    "POST /card-fees" - {
      "should redirect to the enter email address page" in {
        val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/card-fees").withSessionId()
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.testPfSaJourneyCreated)
        val result = systemUnderTest.submit(fakeRequest)
        redirectLocation(result) shouldBe Some("/pay-by-card/email-address")
      }
    }

    "paymentMethodToBeShown" - {
      "should return true if the payment method passed in is within the list of provided payment methods" in {
        val result = systemUnderTest.paymentMethodToBeShown(
          PaymentMethods.OpenBanking, Set(PaymentMethods.OneOffDirectDebit, PaymentMethods.OpenBanking)
        )
        result shouldBe true
      }
      "should return false if the payment method passed in is not within the list of provided payment methods" in {
        val result = systemUnderTest.paymentMethodToBeShown(
          PaymentMethods.OpenBanking, Set(PaymentMethods.OneOffDirectDebit, PaymentMethods.Card)
        )
        result shouldBe false
      }
    }

    "linksAvailableOnFeesPage" - {

      val expectedOpenBankingLink = Link(
        href       = Call("GET", "/pay-by-card/start-open-banking"),
        linkId     = "open-banking-link",
        messageKey = "card-fees.para2.open-banking"
      )

      val expectedBankTransferLink = Link(
        href       = Call("GET", "http://localhost:9056/pay/bac"),
        linkId     = "bank-transfer-link",
        messageKey = "card-fees.para2.bank-transfer"
      )

      val expectedOneOffDirectDebitLink = Link(
        href       = Call("GET", "http://localhost:9056/pay/pay-by-one-off-direct-debit"),
        linkId     = "one-off-direct-debit-link",
        messageKey = "card-fees.para2.one-off-direct-debit"
      )

      "should return the correct links for each origin" in {
        Origins.values.foreach { origin =>
          val expectedLinks = origin match {
            case Origins.PfSa                     => Seq(expectedOpenBankingLink, expectedOneOffDirectDebitLink)
            case Origins.BtaSa                    => Seq(expectedOpenBankingLink, expectedOneOffDirectDebitLink)
            case Origins.PtaSa                    => Seq(expectedOpenBankingLink, expectedOneOffDirectDebitLink)
            case Origins.ItSa                     => Seq(expectedBankTransferLink)
            case Origins.PfVat                    => Seq.empty
            case Origins.PfCt                     => Seq.empty
            case Origins.PfEpayeNi                => Seq.empty
            case Origins.PfEpayeLpp               => Seq.empty
            case Origins.PfEpayeSeta              => Seq.empty
            case Origins.PfEpayeLateCis           => Seq.empty
            case Origins.PfEpayeP11d              => Seq.empty
            case Origins.PfSdlt                   => Seq.empty
            case Origins.PfCds                    => Seq.empty
            case Origins.PfOther                  => Seq.empty
            case Origins.PfP800                   => Seq.empty
            case Origins.PtaP800                  => Seq.empty
            case Origins.PfClass2Ni               => Seq.empty
            case Origins.PfInsurancePremium       => Seq.empty
            case Origins.PfPsAdmin                => Seq.empty
            case Origins.AppSa                    => Seq.empty
            case Origins.BtaVat                   => Seq.empty
            case Origins.BtaEpayeBill             => Seq.empty
            case Origins.BtaEpayePenalty          => Seq.empty
            case Origins.BtaEpayeInterest         => Seq.empty
            case Origins.BtaEpayeGeneral          => Seq.empty
            case Origins.BtaClass1aNi             => Seq.empty
            case Origins.BtaCt                    => Seq.empty
            case Origins.BtaSdil                  => Seq.empty
            case Origins.BcPngr                   => Seq.empty
            case Origins.Parcels                  => Seq.empty
            case Origins.DdVat                    => Seq.empty
            case Origins.DdSdil                   => Seq.empty
            case Origins.VcVatReturn              => Seq.empty
            case Origins.VcVatOther               => Seq.empty
            case Origins.Amls                     => Seq.empty
            case Origins.Ppt                      => Seq.empty
            case Origins.PfCdsCash                => Seq.empty
            case Origins.PfPpt                    => Seq.empty
            case Origins.PfSpiritDrinks           => Seq.empty
            case Origins.PfInheritanceTax         => Seq.empty
            case Origins.Mib                      => Seq.empty
            case Origins.PfClass3Ni               => Seq.empty
            case Origins.PfWineAndCider           => Seq.empty
            case Origins.PfBioFuels               => Seq.empty
            case Origins.PfAirPass                => Seq.empty
            case Origins.PfMgd                    => Seq.empty
            case Origins.PfBeerDuty               => Seq.empty
            case Origins.PfGamingOrBingoDuty      => Seq.empty
            case Origins.PfGbPbRgDuty             => Seq.empty
            case Origins.PfLandfillTax            => Seq.empty
            case Origins.PfSdil                   => Seq.empty
            case Origins.PfAggregatesLevy         => Seq.empty
            case Origins.PfClimateChangeLevy      => Seq.empty
            case Origins.PfSimpleAssessment       => Seq.empty
            case Origins.PtaSimpleAssessment      => Seq.empty
            case Origins.AppSimpleAssessment      => Seq.empty
            case Origins.PfTpes                   => Seq.empty
            case Origins.CapitalGainsTax          => Seq.empty
            case Origins.EconomicCrimeLevy        => Seq.empty
            case Origins.PfEconomicCrimeLevy      => Seq.empty
            case Origins.PfJobRetentionScheme     => Seq.empty
            case Origins.JrsJobRetentionScheme    => Seq.empty
            case Origins.PfImportedVehicles       => Seq.empty
            case Origins.PfChildBenefitRepayments => Seq.empty
            case Origins.NiEuVatOss               => Seq.empty
            case Origins.PfNiEuVatOss             => Seq.empty
            case Origins.NiEuVatIoss              => Seq.empty
            case Origins.PfNiEuVatIoss            => Seq.empty
            case Origins.PfAmls                   => Seq.empty
            case Origins.PfAted                   => Seq.empty
            case Origins.PfCdsDeferment           => Seq.empty
            case Origins.PfTrust                  => Seq.empty
            case Origins.PtaClass3Ni              => Seq.empty
            case Origins.AlcoholDuty              => Seq.empty
            case Origins.PfAlcoholDuty            => Seq.empty
            case Origins.VatC2c                   => Seq.empty
            case Origins.`3psSa`                  => Seq.empty
          }
          systemUnderTest.linksAvailableOnFeesPage(origin) shouldBe expectedLinks withClue s"links did not match expected for origin: ${origin.entryName}"
        }
      }
    }
  }
}
