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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import org.jsoup.Jsoup
import payapi.corcommon.model.JourneyId
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.Address
import uk.gov.hmrc.cardpaymentfrontend.testsupport.ItSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestOps.FakeRequestOps
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.PayApiStub
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestJourneys

import scala.jdk.CollectionConverters.ListHasAsScala

class AddressControllerSpec extends ItSpec {

  val systemUnderTest: AddressController = app.injector.instanceOf[AddressController]

  override def beforeEach(): Unit = {
    PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
    wireMockServer.resetRequests()
    ()
  }

  "AddressController" - {

    "GET /address" - {
      val fakeGetRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/address").withSessionId()
      val fakeGetRequestInWelsh                               = fakeGetRequest.withLangWelsh()

      "should return 200 OK" in {
        val result = systemUnderTest.renderPage(fakeGetRequest)
        status(result) shouldBe Status.OK
      }

      "include the hmrc layout" in {
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("html").hasClass("govuk-template") shouldBe true withClue "no govuk template"
      }

      "show the language toggle" in {
        val result                       = systemUnderTest.renderPage(fakeGetRequest)
        val document                     = Jsoup.parse(contentAsString(result))
        val langToggleText: List[String] = document.select(".hmrc-language-select__list-item").eachText().asScala.toList
        langToggleText should contain theSameElementsAs List("English", "Newid yr iaith i’r Gymraeg Cymraeg") // checking the visually hidden text, it's simpler
      }

      "show the Title tab correctly in English" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.title shouldBe "Card billing address - Pay your Self Assessment - GOV.UK"
      }

      "show the Title tab correctly in Welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.title shouldBe "Cyfeiriad bilio’r cerdyn - Talu eich Hunanasesiad - GOV.UK"
      }

      "show the Service Name banner title correctly in English" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select(".govuk-header__service-name").html shouldBe "Pay your Self Assessment"
      }

      "show the Service Name banner title correctly in Welsh" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select(".govuk-header__service-name").html shouldBe "Talu eich Hunanasesiad"
      }

      "show the heading correctly in English" in {
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("h1").html shouldBe "Card billing address" withClue "Page heading wrong in English"
      }

      "show the heading correctly in Welsh" in {
        val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.select("h1").html shouldBe "Cyfeiriad bilio’r cerdyn" withClue "Page heading wrong in welsh"
      }

      "show the hint correctly in English" in {
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[1]") contains
          "This billing address must match the address your card is registered with." withClue "Page hint 1 wrong in English"
        document.selectXpath("//*[@id=\"main-content\"]/div/div/p[2]") contains
          "If it does not, the payment will fail." withClue "Page hint 2 wrong in English"
      }

      "show the hint correctly in Welsh" in {
        val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document.selectXpath(
          "//*[@id=\"main-content\"]/div/div/p[1]"
        ) contains "Eich cyfeiriad bilio yw’r cyfeiriad y gwnaethoch gofrestru eich cerdyn ag ef" withClue "Page hint wrong in welsh"
      }

      "be prepopulated if there is an address in the session" in {
        def fakeRequestWithAddressInSession(journeyId: JourneyId = TestJourneys.PfSa.journeyBeforeBeginWebPayment._id): FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest()
            .withSessionId()
            .withAddressInSession(journeyId)
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.PfSa.journeyBeforeBeginWebPayment)

        val result   = systemUnderTest.renderPage(fakeRequestWithAddressInSession())
        val document = Jsoup.parse(contentAsString(result))

        document.select("input[name=line1]").attr("value") shouldBe "line1"
        document.select("input[name=line2]").attr("value") shouldBe "line2"
        document.select("input[name=city]").attr("value") shouldBe "city"
        document.select("input[name=postcode]").attr("value") shouldBe "AA0AA0"
        document.select("input[name=county]").attr("value") shouldBe "county"
        document.select("select[name=country] option[selected]").attr("value") shouldBe "GBR"
      }

      "should render custom content on address page when journey is for Mib" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.Mib.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#main-content > div > div > fieldset > form > div:nth-child(6) > label").text() shouldBe "Postcode (optional for non-UK addresses)"
      }

      "should render custom content in welsh on address page when journey is for Mib" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.Mib.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document
          .select("#main-content > div > div > fieldset > form > div:nth-child(6) > label")
          .text() shouldBe "Cod post (dewisol ar gyfer cyfeiriadau y tu allan i’r DU)"
      }

      "should render custom content on address page when journey is for BcPngr" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.BcPngr.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequest)
        val document = Jsoup.parse(contentAsString(result))
        document.select("#main-content > div > div > fieldset > form > div:nth-child(6) > label").text() shouldBe "Postcode (optional for non-UK addresses)"
      }

      "should render custom content in welsh on address page when journey is for BcPngr" in {
        PayApiStub.stubForFindBySessionId2xx(TestJourneys.BcPngr.journeyBeforeBeginWebPayment)
        val result   = systemUnderTest.renderPage(fakeGetRequestInWelsh)
        val document = Jsoup.parse(contentAsString(result))
        document
          .select("#main-content > div > div > fieldset > form > div:nth-child(6) > label")
          .text() shouldBe "Cod post (dewisol ar gyfer cyfeiriadau y tu allan i’r DU)"
      }

    }

    "POST /address" - {
      def fakePostRequest(formData: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
        FakeRequest("POST", "/address").withFormUrlEncodedBody(formData: _*).withSessionId()

      def fakePostRequestInWelsh(formData: (String, String)*): FakeRequest[AnyContentAsFormUrlEncoded] =
        fakePostRequest(formData: _*).withLangWelsh()

      "should return 303 SEE_OTHER and redirect to /check-your-details when a valid address is submitted" in {
        val address = List(
          ("line1", "20 Fake Cottage"),
          ("line2", "Fake Street"),
          ("city", "Imaginaryshire"),
          ("county", "East Imaginationland"),
          ("postcode", "IM2 4HJ"),
          ("country", "GBR")
        )
        val result  = systemUnderTest.submit(fakePostRequest(address: _*))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/pay-by-card/check-your-details")
      }

      "should call pay-api and reset order when journey is in Sent state and address submitted is different to what is already in session" in {
        val testJourney         = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val address             = List(
          ("line1", "20 Fake Cottage"),
          ("line2", "Fake Street"),
          ("city", "Imaginaryshire"),
          ("county", "East Imaginationland"),
          ("postcode", "IM2 4HJ"),
          ("country", "GBR")
        )
        PayApiStub.stubForFindBySessionId2xx(testJourney)
        PayApiStub.stubForResetWebPayment2xx(testJourney._id)
        val result              = systemUnderTest.submit(fakePostRequest(address: _*).withAddressInSession(testJourney._id))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/pay-by-card/check-your-details")
        val addressInSession    = session(result).get(testJourney._id.value)
        val expectedAddressJson = Json.parse(
          """{
            |"address" : {
            |    "line1" : "20 Fake Cottage",
            |    "line2" : "Fake Street",
            |    "city" : "Imaginaryshire",
            |    "county" : "East Imaginationland",
            |    "postcode" : "IM2 4HJ",
            |    "country" : "GBR"
            |  }
            |}
            |""".stripMargin
        )
        addressInSession.map(Json.parse) shouldBe Some(expectedAddressJson)
        PayApiStub.verifyResetWebPayment(1, testJourney._id)
      }

      "should not have to call pay-api to reset webpayment when re submitted address is not different to the one used before" in {
        val testJourney     = TestJourneys.PfSa.journeyAfterBeginWebPayment
        val testAddress     = Address(line1 = "line1", postcode = Some("AA11AA"), country = "GBR")
        val testAddressList = List(
          ("line1", testAddress.line1),
          ("postcode", testAddress.postcode.getOrElse(throw new RuntimeException("expecting postcode in test data for this test"))),
          ("country", testAddress.country)
        )
        PayApiStub.stubForFindBySessionId2xx(testJourney)
        val result          = systemUnderTest.submit(fakePostRequest(testAddressList: _*).withAddressInSession(testJourney._id, testAddress))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/pay-by-card/check-your-details")
        PayApiStub.verifyResetWebPayment(0, testJourney._id)
      }

      "should not have to call pay-api to reset webpayment when order is null in journey" in {
        val testJourney = TestJourneys.PfSa.journeyBeforeBeginWebPayment
        val testAddress = Address(line1 = "line1", postcode = Some("AA11AA"), country = "GBR")
        val address     = List(
          ("line1", "line1updated"),
          ("postcode", "AA11AA"),
          ("country", "GBR")
        )
        PayApiStub.stubForFindBySessionId2xx(testJourney)
        val result      = systemUnderTest.submit(fakePostRequest(address: _*).withAddressInSession(testJourney._id, testAddress))
        status(result) shouldBe Status.SEE_OTHER
        redirectLocation(result) shouldBe Some("/pay-by-card/check-your-details")
        PayApiStub.verifyResetWebPayment(0, testJourney._id)
      }

      "Should show the correct error title content in English" in {
        val address  = List(
          ("line1", ""),
          ("line2", "Fake Street"),
          ("city", "Imaginaryshire"),
          ("county", "East Imaginationland"),
          ("postcode", "IM2 4HJ"),
          ("country", "GBR")
        )
        val result   = systemUnderTest.submit(fakePostRequest(address: _*))
        val document = Jsoup.parse(contentAsString(result))
        document.title() shouldBe "Error: Card billing address - Pay your Self Assessment - GOV.UK"
      }

      "Should show the correct error title content in Welsh" in {
        val address  = List(
          ("line1", ""),
          ("line2", "Fake Street"),
          ("city", "Imaginaryshire"),
          ("county", "East Imaginationland"),
          ("postcode", "IM2 4HJ"),
          ("country", "GBR")
        )
        val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
        val document = Jsoup.parse(contentAsString(result))
        document.title() shouldBe "Gwall: Cyfeiriad bilio’r cerdyn - Talu eich Hunanasesiad - GOV.UK"
      }

      "for first line of address" - {
        "should return html containing the correct error messages when first line of address is missing" in {
          val address  = List(
            ("line1", ""),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter the first line of the billing address"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#line1"
          document.select("#line1-error").text() shouldBe "Error: Enter the first line of the billing address"
        }

        "should return html containing the correct error messages when first line of address is missing in welsh" in {
          val address  = List(
            ("line1", ""),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch linell gyntaf y cyfeiriad bilio"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#line1"
          document.select("#line1-error").text() shouldBe "Gwall: Nodwch linell gyntaf y cyfeiriad bilio"
        }

        "should return html containing the correct error messages when first line of address is more than 100 characters" in {
          val address  = List(
            ("line1", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter the first line of your address using no more than 100 characters"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#line1"
          document.select("#line1-error").text() shouldBe "Error: Enter the first line of your address using no more than 100 characters"
        }

        "should return html containing the correct error messages when first line of address is more than 100 characters in welsh" in {
          val address  = List(
            ("line1", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch linell gyntaf eich cyfeiriad gan beidio â defnyddio mwy na 100 o gymeriadau"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#line1"
          document.select("#line1-error").text() shouldBe "Gwall: Nodwch linell gyntaf eich cyfeiriad gan beidio â defnyddio mwy na 100 o gymeriadau"
        }
      }

      "for second line of address" - {
        "should return html containing the correct error messages when second line of address is more than 100 characters" in {
          val address  = List(
            ("line1", "Fake Street"),
            ("line2", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter the second line of your address using no more than 100 characters"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#line2"
          document.select("#line2-error").text() shouldBe "Error: Enter the second line of your address using no more than 100 characters"
        }

        "should return html containing the correct error messages when second line of address is more than 100 characters in welsh" in {
          val address  = List(
            ("line1", "Fake Street"),
            ("line2", "12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch ail linell eich cyfeiriad gan beidio â defnyddio mwy na 100 o gymeriadau"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#line2"
          document.select("#line2-error").text() shouldBe "Gwall: Nodwch ail linell eich cyfeiriad gan beidio â defnyddio mwy na 100 o gymeriadau"
        }
      }

      "for city" - {
        "should return html containing the correct error messages when city is more than 50 characters" in {
          val address  = List(
            ("line1", "Fake Street"),
            ("city", "123456789012345678901234567890123456789012345678901"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter your city using no more than 50 characters"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#city"
          document.select("#city-error").text() shouldBe "Error: Enter your city using no more than 50 characters"
        }

        "should return html containing the correct error messages when city is more than 50 characters in welsh" in {
          val address  = List(
            ("line1", "Fake Street"),
            ("city", "123456789012345678901234567890123456789012345678901"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch eich dinas gan beidio â defnyddio mwy na 50 o gymeriadau"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#city"
          document.select("#city-error").text() shouldBe "Gwall: Nodwch eich dinas gan beidio â defnyddio mwy na 50 o gymeriadau"
        }
      }

      "for county" - {
        "should return html containing the correct error messages when county is more than 50 characters" in {
          val address  = List(
            ("line1", "Fake Street"),
            ("county", "123456789012345678901234567890123456789012345678901"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter your county using no more than 50 characters"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#county"
          document.select("#county-error").text() shouldBe "Error: Enter your county using no more than 50 characters"
        }

        "should return html containing the correct error messages when county is more than 50 characters in welsh" in {
          val address  = List(
            ("line1", "Fake Street"),
            ("county", "123456789012345678901234567890123456789012345678901"),
            ("postcode", "IM2 4HJ"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch eich sir gan beidio â defnyddio mwy na 50 o gymeriadau"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#county"
          document.select("#county-error").text() shouldBe "Gwall: Nodwch eich sir gan beidio â defnyddio mwy na 50 o gymeriadau"
        }
      }

      "for postcode" - {
        "should return html containing the correct error messages when postcode is missing" in {
          val address  = List(
            ("line1", "20 Fake Cottage"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", ""),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter your postcode"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#postcode"
          document.select("#postcode-error").text() shouldBe "Error: Enter your postcode"
        }

        "should return html containing the correct error messages when postcode is missing in welsh" in {
          val address  = List(
            ("line1", "20 Fake Cottage"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", ""),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch eich cod post"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#postcode"
          document.select("#postcode-error").text() shouldBe "Gwall: Nodwch eich cod post"
        }

        "should return html containing the correct error messages when postcode does not match regex" in {
          val address  = List(
            ("line1", "20 Fake Cottage"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "🍗"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Enter your postcode in the correct format"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#postcode"
          document.select("#postcode-error").text() shouldBe "Error: Enter your postcode in the correct format"
        }

        "should return html containing the correct error messages when postcode does not match regex in welsh" in {
          val address  = List(
            ("line1", "20 Fake Cottage"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "🍗"),
            ("country", "GBR")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Nodwch eich cod post yn y fformat"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#postcode"
          document.select("#postcode-error").text() shouldBe "Gwall: Nodwch eich cod post yn y fformat"
        }
      }

      "for country" - {
        "should return html containing the correct error messages when no country selected" in {
          val address  = List(
            ("line1", "20 Fake Cottage"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "")
          )
          val result   = systemUnderTest.submit(fakePostRequest(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "There is a problem"
          document.select(".govuk-error-summary__list").text() shouldBe "Select a country"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#country"
          document.select("#country-error").text() shouldBe "Error: Select a country"
        }

        "should return html containing the correct error messages when no country selected in welsh" in {
          val address  = List(
            ("line1", "20 Fake Cottage"),
            ("line2", "Fake Street"),
            ("city", "Imaginaryshire"),
            ("county", "East Imaginationland"),
            ("postcode", "IM2 4HJ"),
            ("country", "")
          )
          val result   = systemUnderTest.submit(fakePostRequestInWelsh(address: _*))
          val document = Jsoup.parse(contentAsString(result))
          document.select(".govuk-error-summary__title").text() shouldBe "Mae problem wedi codi"
          document.select(".govuk-error-summary__list").text() shouldBe "Dewiswch eich gwlad"
          document.select(".govuk-error-summary__list").select("a").attr("href") shouldBe "#country"
          document.select("#country-error").text() shouldBe "Gwall: Dewiswch eich gwlad"
        }
      }

      "should return a 400 BAD_REQUEST when an invalid address is submitted" in {
        val address = List(
          ("line1", "20 Fake Cottage"),
          ("line2", "Fake Street"),
          ("city", "Imaginaryshire"),
          ("county", "East Imaginationland"),
          ("postcode", "IM2 4HJ"),
          ("country", "")
        )
        val result  = systemUnderTest.submit(fakePostRequest(address: _*))
        status(result) shouldBe Status.BAD_REQUEST
      }

    }

    "addressInSession" - {
      "should return Some[Address] when there is one in session and it's associated with the 'address' key" in {
        val fakeRequest    = FakeRequest("GET", "/blah").withAddressInSession(TestJourneys.PfSa.journeyBeforeBeginWebPayment._id)
        val journeyRequest = new JourneyRequest(TestJourneys.PfSa.journeyBeforeBeginWebPayment, fakeRequest)
        val result         = systemUnderTest.addressInSession(journeyRequest)
        result shouldBe Some(Address("line1", Some("line2"), Some("city"), Some("county"), Some("AA0AA0"), "GBR"))
      }
      "should return None when there is not one in session associated with the 'address' key" in {
        val fakeRequest    = FakeRequest("GET", "/blah")
        val journeyRequest = new JourneyRequest(TestJourneys.PfSa.journeyBeforeBeginWebPayment, fakeRequest)
        val result         = systemUnderTest.addressInSession(journeyRequest)
        result shouldBe None
      }
    }

    "addressIsDifferent" - {
      "should return true when two different addresses are provided" in {
        systemUnderTest.addressIsDifferent(
          Address(line1 = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = Some("postcode"), country = "country"),
          Address(
            line1 = "line1butdifferent",
            line2 = Some("line2"),
            city = Some("city"),
            county = Some("county"),
            postcode = Some("postcode"),
            country = "country"
          )
        ) shouldBe true
      }
      "should return false when two identical addresses are provided" in {
        systemUnderTest.addressIsDifferent(
          Address(line1 = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = Some("postcode"), country = "country"),
          Address(line1 = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = Some("postcode"), country = "country")
        ) shouldBe false
      }
    }

  }

}
