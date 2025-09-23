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

package uk.gov.hmrc.cardpaymentfrontend.testsupport

import com.google.inject.{AbstractModule, Provides, Singleton}
import org.openqa.selenium.WebDriver
import org.scalatest.freespec.AnyFreeSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerTest
import org.scalatestplus.play.{HtmlUnitFactory, OneBrowserPerTest}
import payapi.corcommon.model.TransNumberGenerator
import play.api.Application
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.mockclasses.MockTransNumberGenerator
import uk.gov.hmrc.cardpaymentfrontend.testsupport.stubs.AuditConnectorStub
import uk.gov.hmrc.http.test.WireMockSupport

import java.time._
import java.time.format.DateTimeFormatter
import scala.annotation.unused

/**
 * To only be used in specific circumstances when you cannot test a feature with conventional unitspec/itspecs - i.e. you need a browser of some kind.
 */
trait BrowserSpec extends AnyFreeSpecLike
  with GuiceOneServerPerTest
  with OneBrowserPerTest
  with HtmlUnitFactory
  with WireMockSupport
  with RichMatchers { self =>

  implicit val driver: WebDriver = HtmlUnitFactory.createWebDriver(enableJavascript = true)

  override def beforeEach(): Unit = {
    super.beforeEach()
    AuditConnectorStub.stubForImplicitAudit()
    driver.manage().deleteAllCookies()
    ()
  }

  override def afterAll(): Unit = {
    super.afterEach()
    driver.close()
  }

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .overrides(GuiceableModule.fromGuiceModules(Seq(testModule)))
      .configure(configMap)
      .build()
  }

  protected lazy val configOverrides: Map[String, Any] = Map()

  protected lazy val configMap: Map[String, Any] = Map[String, Any](
    "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes",
    "auditing.consumer.baseUri.port" -> self.wireMockPort,
    "auditing.enabled" -> false,
    "auditing.traceRequests" -> false,
    "microservice.services.card-payment.port" -> self.wireMockPort,
    "microservice.services.email-service.port" -> self.wireMockPort,
    "microservice.services.open-banking.port" -> self.wireMockPort,
    "microservice.services.pay-api.port" -> self.wireMockPort,
    "microservice.services.payments-survey.port" -> self.wireMockPort,
    "internal-auth.token" -> "testToken"
  ) ++ configOverrides

  private lazy val testModule: AbstractModule = new AbstractModule {

    @Provides
    @Singleton
    @unused
    def transNumberGenerator(): TransNumberGenerator = new MockTransNumberGenerator

    @Provides
    @Singleton
    @unused
    def clock(): Clock = FrozenTime.clock
  }

  object FrozenTime {
    lazy val dateString: String = "2059-11-25"
    lazy val timeString: String = s"${dateString}T16:33:51.880"
    lazy val localDateTime: LocalDateTime = {
      //the frozen time has to be in future otherwise things will disappear from mongodb because of ttl
      LocalDateTime.parse(timeString, DateTimeFormatter.ISO_DATE_TIME)
    }
    lazy val instant: Instant = localDateTime.toInstant(ZoneOffset.UTC)
    lazy val frozenInstant: Instant = instant
    lazy val clock: Clock = Clock.fixed(frozenInstant, ZoneId.of("UTC"))
  }

  //helper methods
  def clickById(id: String): Unit = clickOn(IdQuery(id))
  def setTextValue(id: String, value: String): Unit = find(IdQuery(id)).foreach(_.underlying.sendKeys(value))
  def setSelectValue(id: String, value: String): Unit = singleSel(IdQuery(id)).value = value

}
