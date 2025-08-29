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

import org.scalatest.AppendedClues.convertToClueful
import org.scalatest.prop.{TableDrivenPropertyChecks, TableFor2}
import payapi.cardpaymentjourney.model.journey._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.TestHelpers.implementedOrigins
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{JourneyStatuses, TestJourneys}

class ExternalNavigationSpec extends UnitSpec with TableDrivenPropertyChecks {

  "returnUrlCancelled" - {
    val someUrl = Some(Url("https://www.return-url.com"))

    type JsdBounds = JsdPfEpayeNi with JsdAlcoholDuty with JsdPfPpt with JsdBtaEpayeBill with JsdPfSa with JsdPpt with JsdVcVatReturn with JsdPfEpayeP11d with JsdCapitalGainsTax with JsdBtaCt with JsdPfEpayeLateCis with JsdPfEpayeLpp with JsdPfCt with JsdBtaVat with JsdPfSdlt with JsdBtaEpayeInterest with JsdAmls with JsdBtaEpayeGeneral with JsdPfEpayeSeta with JsdPtaSa with JsdBtaClass1aNi with JsdVcVatOther with JsdPfAmls with JsdPfVat with JsdItSa with JsdBtaSa with JsdPfAlcoholDuty with JsdBtaEpayePenalty with JsdEconomicCrimeLevy with JsdPfEconomicCrimeLevy with JsdWcSa with JsdWcCt with JsdWcVat with JsdVatC2c with JsdPfVatC2c with JsdWcSimpleAssessment with JsdWcXref with JsdWcEpayeLpp

    val scenarios: TableFor2[JourneyStatuses[_ >: JsdBounds <: JourneySpecificData], Option[Url]] = Table(
      ("journey", "expectedUrl"),
      //returnUrls are set in TestJourneys
      //Logged out journeys, Logged out journeys should return None
      (TestJourneys.PfSa, None),
      (TestJourneys.PfCt, None),
      (TestJourneys.PfEpayeNi, None),
      (TestJourneys.PfEpayeLpp, None),
      (TestJourneys.PfEpayeSeta, None),
      (TestJourneys.PfEpayeLateCis, None),
      (TestJourneys.PfEpayeP11d, None),
      (TestJourneys.PfAlcoholDuty, None),
      (TestJourneys.PfVat, None),
      (TestJourneys.PfPpt, None),
      (TestJourneys.PfAmls, None),
      (TestJourneys.PfSdlt, None),
      (TestJourneys.PfEconomicCrimeLevy, None),
      (TestJourneys.WcSa, None),
      (TestJourneys.WcCt, None),
      (TestJourneys.WcVat, None),
      (TestJourneys.WcXref, None),
      (TestJourneys.PfVatC2c, None),
      (TestJourneys.VatC2c, None),
      (TestJourneys.WcSimpleAssessment, None),
      (TestJourneys.WcEpayeLpp, None),

      //Logged in journeys, Logged out journeys will return what ever the calling services sets
      (TestJourneys.BtaSa, someUrl),
      (TestJourneys.BtaCt, someUrl),
      (TestJourneys.PtaSa, someUrl),
      (TestJourneys.ItSa, someUrl),
      (TestJourneys.AlcoholDuty, someUrl),
      (TestJourneys.BtaVat, someUrl),
      (TestJourneys.VcVatOther, someUrl),
      (TestJourneys.VcVatReturn, someUrl),
      (TestJourneys.BtaEpayeBill, someUrl),
      (TestJourneys.BtaEpayePenalty, someUrl),
      (TestJourneys.BtaEpayeGeneral, someUrl),
      (TestJourneys.BtaEpayeInterest, someUrl),
      (TestJourneys.BtaClass1aNi, someUrl),
      (TestJourneys.Ppt, someUrl),
      (TestJourneys.Amls, someUrl),
      (TestJourneys.CapitalGainsTax, someUrl),
      (TestJourneys.EconomicCrimeLevy, someUrl)
    )

    forAll(scenarios) { (journey, expectedUrl) =>
      val urlValue = expectedUrl.map(_.value).getOrElse("None")
      s"for ${journey.journeyAfterCancelWebPayment.origin.entryName} journey, should return $urlValue" in {
        ExternalNavigation.returnUrlCancelled(journey.journeyAfterCancelWebPayment) shouldBe expectedUrl
      }
    }

    implementedOrigins.foreach { origin =>
      s"for journey with origin ${origin.entryName}, test scenario should exist" in {
        scenarios.exists { scenario =>
          scenario._1.journeyAfterCancelWebPayment.origin == origin
        } shouldBe true withClue s"No test scenario found for origin: ${origin.entryName}"
      }
    }
  }
}
