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

package uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata

import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.taxes.ad.{AlcoholDutyChargeReference, AlcoholDutyReference}
import payapi.corcommon.model.taxes.ct.{CtChargeTypes, CtLivePeriod, CtPeriod, CtUtr}
import payapi.corcommon.model.taxes.epaye.{AccountsOfficeReference, PsaNumber, QuarterlyEpayeTaxPeriod, YearlyEpayeTaxPeriod}
import payapi.corcommon.model.taxes.other.{XRef, XRef14Char}
import payapi.corcommon.model.taxes.sa.SaUtr
import payapi.corcommon.model.times.period.TaxQuarter.AprilJuly
import payapi.corcommon.model.times.period.TaxYear
import payapi.corcommon.model.{AmountInPence, JourneyId, PaymentStatuses}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestDataUtils._

import java.time.{LocalDate, LocalDateTime}

sealed trait JourneyStatuses[jsd <: JourneySpecificData] {
  def journeyBeforeBeginWebPayment: Journey[jsd]
  def journeyAfterBeginWebPayment: Journey[jsd] = intoSentWithOrder[jsd](journeyBeforeBeginWebPayment, sentOrder)
  def journeyAfterSucceedDebitWebPayment: Journey[jsd] = intoSuccessWithOrder[jsd](journeyAfterBeginWebPayment, debitCardOrder)
  def journeyAfterSucceedCreditWebPayment: Journey[jsd] = intoSuccessWithOrder[jsd](journeyAfterBeginWebPayment, creditCardOrder)
  def journeyAfterFailWebPayment: Journey[jsd] = intoFailed[jsd](journeyAfterBeginWebPayment, None) //todo failed order, it wasn never done properly.
  def journeyAfterCancelWebPayment: Journey[jsd] = intoCancelled[jsd](journeyAfterBeginWebPayment, None) //likewise with cancelled
}

object TestJourneys {

  object PfSa extends JourneyStatuses[JsdPfSa] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfSa] = Journey[JsdPfSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = None,
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfSa(utr = Some(SaUtr("1234567895"))),
      chosenWayToPay       = None
    )
  }

  object BtaSa extends JourneyStatuses[JsdBtaSa] {
    val journeyBeforeBeginWebPayment: Journey[JsdBtaSa] = Journey[JsdBtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-bta.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = AmountInPence(1234), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )
  }

  object PtaSa extends JourneyStatuses[JsdPtaSa] {
    val journeyBeforeBeginWebPayment: Journey[JsdPtaSa] = Journey[JsdPtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-pta.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = Some(AmountInPence(1234)), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )
  }

  object ItSa extends JourneyStatuses[JsdItSa] {
    val journeyBeforeBeginWebPayment: Journey[JsdItSa] = Journey[JsdItSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-itsa.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdItSa(utr     = SaUtr("1234567895"), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )
  }

  object PfAlcoholDuty extends JourneyStatuses[JsdPfAlcoholDuty] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfAlcoholDuty] = Journey[JsdPfAlcoholDuty](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = None,
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfAlcoholDuty(alcoholDutyReference = Some(AlcoholDutyReference("XMADP0123456789"))),
      chosenWayToPay       = None
    )
  }

  object AlcoholDuty extends JourneyStatuses[JsdAlcoholDuty] {
    val journeyBeforeBeginWebPayment: Journey[JsdAlcoholDuty] = Journey[JsdAlcoholDuty](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-alcoholduty.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdAlcoholDuty(
        alcoholDutyReference       = AlcoholDutyReference("XMADP0123456789"),
        alcoholDutyChargeReference = Some(AlcoholDutyChargeReference("XE1234567890123")),
        defaultAmountInPence       = AmountInPence(1234)
      ),
      chosenWayToPay       = None
    )
  }

  object BtaCt extends JourneyStatuses[JsdBtaCt] {
    val journeyBeforeBeginWebPayment: Journey[JsdBtaCt] = Journey[JsdBtaCt](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-btact.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaCt(
        utr                  = CtUtr("1097172564"),
        ctPeriod             = Some(CtPeriod(1)),
        ctChargeType         = Some(CtChargeTypes.A),
        defaultAmountInPence = AmountInPence(1234),
        dueDate              = Some(LocalDate.of(2028, 12, 12)),
        ctLivePeriods        = Some(List(CtLivePeriod(1, LocalDate.of(2028, 11, 12), LocalDate.of(2028, 12, 12))))
      ),
      chosenWayToPay       = None
    )
  }

  object PfCt extends JourneyStatuses[JsdPfCt] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfCt] = Journey[JsdPfCt](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = None,
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfCt(
        utr          = Some(CtUtr("1097172564")),
        ctPeriod     = Some(CtPeriod(1)),
        ctChargeType = Some(CtChargeTypes.A)
      ),
      chosenWayToPay       = None
    )
  }

  object PfEpayeNi extends JourneyStatuses[JsdPfEpayeNi] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfEpayeNi] = Journey[JsdPfEpayeNi](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-pfepayeni.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfEpayeNi(
        Some(AccountsOfficeReference("123PH45678900")),
        Some(QuarterlyEpayeTaxPeriod(AprilJuly, TaxYear(2025)))
      ),
      chosenWayToPay       = None
    )
  }

  object PfEpayeP11d extends JourneyStatuses[JsdPfEpayeP11d] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfEpayeP11d] = Journey[JsdPfEpayeP11d](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-pfepayep11d.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfEpayeP11d(
        Some(AccountsOfficeReference("123PH45678900")),
        Some(YearlyEpayeTaxPeriod(TaxYear(2025)))
      ),
      chosenWayToPay       = None
    )
  }

  object PfEpayeLpp extends JourneyStatuses[JsdPfEpayeLpp] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfEpayeLpp] = Journey[JsdPfEpayeLpp](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-pfepayelpp.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfEpayeLpp(prn = Some(XRef("XE123456789012"))),
      chosenWayToPay       = None
    )
  }

  object PfEpayeLateCis extends JourneyStatuses[JsdPfEpayeLateCis] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfEpayeLateCis] = Journey[JsdPfEpayeLateCis](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-pfepayelatecis.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfEpayeLateCis(prn = Some(XRef14Char("123PH45678900"))),
      chosenWayToPay       = None
    )
  }

  object PfEpayeSeta extends JourneyStatuses[JsdPfEpayeSeta] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfEpayeSeta] = Journey[JsdPfEpayeSeta](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-pfepayeseta.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfEpayeSeta(psaNumber = Some(PsaNumber("XA123456789012"))),
      chosenWayToPay       = None
    )
  }

}
