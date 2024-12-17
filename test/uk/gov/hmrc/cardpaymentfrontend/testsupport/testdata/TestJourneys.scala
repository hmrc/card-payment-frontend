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

import payapi.cardpaymentjourney.model.barclays.BarclaysOrder
import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.barclays.{CardCategories, TransactionReference}
import payapi.corcommon.model.taxes.ad.{AlcoholDutyChargeReference, AlcoholDutyReference}
import payapi.corcommon.model.taxes.sa.SaUtr
import payapi.corcommon.model.{AmountInPence, JourneyId, PaymentStatuses}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.TestDataUtils.{creditCardOrder, debitCardOrder, intoFailed, intoSuccessWithOrder}

import java.time.{LocalDate, LocalDateTime}

object TestJourneys {

  object PfSa {
    val testPfSaJourneyCreated: Journey[JsdPfSa] = Journey[JsdPfSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = None,
      emailTemplateOptions = None,
      navigation           = None,
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfSa(utr = None),
      chosenWayToPay       = None
    )

    val testPfSaJourneyUpdatedWithRefAndAmount: Journey[JsdPfSa] = Journey[JsdPfSa](
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

    val testPfSaJourneySuccessDebit: Journey[JsdPfSa] = intoSuccessWithOrder[JsdPfSa](testPfSaJourneyUpdatedWithRefAndAmount, debitCardOrder)
    val testPfSaJourneySuccessCredit: Journey[JsdPfSa] = intoSuccessWithOrder[JsdPfSa](testPfSaJourneyUpdatedWithRefAndAmount, creditCardOrder)
    val testPfSaJourneyFailed: Journey[JsdPfSa] = intoFailed[JsdPfSa](testPfSaJourneyUpdatedWithRefAndAmount, None)
  }

  object BtaSa {
    val testBtaSaJourneyUpdatedWithRefAndAmount: Journey[JsdBtaSa] = Journey[JsdBtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-bta.com"), backUrl = Url("https://www.back-to-bta.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = AmountInPence(1234), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )

    val testBtaSaJourneySuccessDebit: Journey[JsdBtaSa] = Journey[JsdBtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-bta.com"), backUrl = Url("https://www.back-to-bta.com"))),
      order                = Some(BarclaysOrder(
        transactionReference = TransactionReference("Some-transaction-ref"),
        iFrameUrl            = Url("some-url"),
        cardCategory         = Some(CardCategories.debit),
        commissionInPence    = None,
        paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
      )),
      status               = PaymentStatuses.Successful,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = AmountInPence(1234), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )

    val testBtaSaJourneySuccessDebitNoDueDate: Journey[JsdBtaSa] = Journey[JsdBtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-bta.com"), backUrl = Url("https://www.back-to-bta.com"))),
      order                = Some(BarclaysOrder(
        transactionReference = TransactionReference("Some-transaction-ref"),
        iFrameUrl            = Url("some-url"),
        cardCategory         = Some(CardCategories.debit),
        commissionInPence    = None,
        paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
      )),
      status               = PaymentStatuses.Successful,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = AmountInPence(1234), dueDate = None),
      chosenWayToPay       = None
    )

    val testBtaSaJourneySuccessDebitOverdue: Journey[JsdBtaSa] = Journey[JsdBtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-bta.com"), backUrl = Url("https://www.back-to-bta.com"))),
      order                = Some(BarclaysOrder(
        transactionReference = TransactionReference("Some-transaction-ref"),
        iFrameUrl            = Url("some-url"),
        cardCategory         = Some(CardCategories.debit),
        commissionInPence    = None,
        paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
      )),
      status               = PaymentStatuses.Successful,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = AmountInPence(1234), dueDate = Some(LocalDate.of(2023, 12, 12))),
      chosenWayToPay       = None
    )

    val testBtaSaJourneySuccessCredit: Journey[JsdBtaSa] = Journey[JsdBtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = None,
      order                = Some(BarclaysOrder(
        transactionReference = TransactionReference("Some-transaction-ref"),
        iFrameUrl            = Url("some-url"),
        cardCategory         = Some(CardCategories.credit),
        commissionInPence    = Some(AmountInPence(123)),
        paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
      )),
      status               = PaymentStatuses.Successful,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdBtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = AmountInPence(1234), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )
  }

  object PtaSa {
    val testPtaSaJourneyUpdatedWithRefAndAmount: Journey[JsdPtaSa] = Journey[JsdPtaSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-pta.com"), backUrl = Url("https://www.back-to-pta.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      futureDatedPayment   = None,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPtaSa(utr                  = SaUtr("1234567895"), defaultAmountInPence = Some(AmountInPence(1234)), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )

    val testPtaSaJourneySuccessDebit: Journey[JsdPtaSa] = intoSuccessWithOrder(testPtaSaJourneyUpdatedWithRefAndAmount, debitCardOrder)
    val testPtaSaJourneySuccessCredit: Journey[JsdPtaSa] = intoSuccessWithOrder(testPtaSaJourneyUpdatedWithRefAndAmount, creditCardOrder)
  }

  object ItSa {
    val testItSaJourneyUpdatedWithRefAndAmount: Journey[JsdItSa] = Journey[JsdItSa](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-itsa.com"), backUrl = Url("https://www.back-to-itsa.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdItSa(utr     = SaUtr("1234567895"), dueDate = Some(LocalDate.of(2028, 12, 12))),
      chosenWayToPay       = None
    )

    val testItSaJourneySuccessDebit: Journey[JsdItSa] = intoSuccessWithOrder(testItSaJourneyUpdatedWithRefAndAmount, debitCardOrder)
    val testItSaJourneySuccessCredit: Journey[JsdItSa] = intoSuccessWithOrder(testItSaJourneyUpdatedWithRefAndAmount, creditCardOrder)
  }

  object PfAlcoholDuty {
    val testPfAlcoholDutyJourneyUpdatedWithRefAndAmount: Journey[JsdPfAlcoholDuty] = Journey[JsdPfAlcoholDuty](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-pfalcoholduty.com"), backUrl = Url("https://www.back-to-pfalcoholduty.com"))),
      order                = None,
      status               = PaymentStatuses.Created,
      createdOn            = LocalDateTime.parse("2027-11-02T16:28:55.185"),
      journeySpecificData  = JsdPfAlcoholDuty(alcoholDutyReference = Some(AlcoholDutyReference("XMADP0123456789"))),
      chosenWayToPay       = None
    )

    val testPfAlcoholDutyJourneySuccessDebit: Journey[JsdPfAlcoholDuty] = intoSuccessWithOrder(testPfAlcoholDutyJourneyUpdatedWithRefAndAmount, debitCardOrder)
    val testPfAlcoholDutyJourneySuccessCredit: Journey[JsdPfAlcoholDuty] = intoSuccessWithOrder(testPfAlcoholDutyJourneyUpdatedWithRefAndAmount, creditCardOrder)
  }

  object AlcoholDuty {
    val testAlcoholDutyJourneyUpdatedWithRefAndAmount: Journey[JsdAlcoholDuty] = Journey[JsdAlcoholDuty](
      _id                  = JourneyId("TestJourneyId-44f9-ad7f-01e1d3d8f151"),
      sessionId            = Some(SessionId("TestSession-4b87460d-6f43-4c4c-b810-d6f87c774854")),
      amountInPence        = Some(AmountInPence(1234)),
      emailTemplateOptions = None,
      navigation           = Some(NavigationOptions(returnUrl = Url("https://www.return-to-pfalcoholduty.com"), backUrl = Url("https://www.back-to-pfalcoholduty.com"))),
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

    val testAlcoholDutyJourneySuccessDebit: Journey[JsdAlcoholDuty] = intoSuccessWithOrder(testAlcoholDutyJourneyUpdatedWithRefAndAmount, debitCardOrder)
    val testAlcoholDutyJourneySuccessCredit: Journey[JsdAlcoholDuty] = intoSuccessWithOrder(testAlcoholDutyJourneyUpdatedWithRefAndAmount, creditCardOrder)
  }

}
