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

package uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata

import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.taxes.ad.{AlcoholDutyChargeReference, AlcoholDutyReference}
import payapi.corcommon.model.taxes.ct.{CtChargeTypes, CtLivePeriod, CtPeriod, CtUtr}
import payapi.corcommon.model.taxes.sa.SaUtr
import payapi.corcommon.model.taxes.vat.{CalendarPeriod, VatChargeReference, Vrn}
import payapi.corcommon.model.{AmountInPence, JourneyId, PaymentStatuses}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestDataUtils._

import java.time.{LocalDate, LocalDateTime}

sealed trait JourneyStatuses[jsd <: JourneySpecificData] {
  def journeyBeforeBeginWebPayment: Journey[jsd]
  def journeyAfterBeginWebPayment: Journey[jsd] = intoSentWithOrder[jsd](journeyBeforeBeginWebPayment, sentOrder)
  def journeyAfterSucceedDebitWebPayment: Journey[jsd] = intoSuccessWithOrder[jsd](journeyAfterBeginWebPayment, debitCardOrder)
  def journeyAfterSucceedCreditWebPayment: Journey[jsd] = intoSuccessWithOrder[jsd](journeyAfterBeginWebPayment, creditCardOrder)
  def journeyAfterFailWebPayment: Journey[jsd] = intoFailed[jsd](journeyAfterBeginWebPayment, None) //todo failed order, it was never done properly.
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

  object PfVat extends JourneyStatuses[JsdPfVat] {
    val journeyBeforeBeginWebPayment: Journey[JsdPfVat] = Journey[JsdPfVat](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = None,
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfVat(
        vrn       = Some(Vrn("999964805")),
        chargeRef = None
      ),
      chosenWayToPay       = None
    )
  }

  object BtaVat extends JourneyStatuses[JsdBtaVat] {
    val journeyBeforeBeginWebPayment: Journey[JsdBtaVat] = Journey[JsdBtaVat](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-btavat.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaVat(vrn                  = Vrn("999964805"), defaultAmountInPence = AmountInPence(1234), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )
  }

  object VcVatReturn extends JourneyStatuses[JsdVcVatReturn] {
    val journeyBeforeBeginWebPayment: Journey[JsdVcVatReturn] = Journey[JsdVcVatReturn](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-vcvatreturn.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdVcVatReturn(
        vrn                  = Vrn("999964805"),
        defaultAmountInPence = AmountInPence(1234),
        dueDate              = Some(LocalDate.of(2028, 12, 12)),
        accountingPeriod     = Some(CalendarPeriod(11, 2027))
      ),
      chosenWayToPay       = None
    )
  }

  object VcVatOther extends JourneyStatuses[JsdVcVatOther] {
    val journeyBeforeBeginWebPayment: Journey[JsdVcVatOther] = Journey[JsdVcVatOther](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-url.com"), backUrl = Url("https://www.back-to-vcvatreturn.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdVcVatOther(
        vrn                  = Vrn("999964805"),
        defaultAmountInPence = AmountInPence(1234),
        dueDate              = Some(LocalDate.of(2028, 12, 12)),
        accountingPeriod     = Some(CalendarPeriod(11, 2027)),
        chargeReference      = VatChargeReference("999964805")
      ),
      chosenWayToPay       = None
    )
  }

}
