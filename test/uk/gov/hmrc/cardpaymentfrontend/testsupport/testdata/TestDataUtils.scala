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

import payapi.cardpaymentjourney.model.barclays.BarclaysOrder
import payapi.cardpaymentjourney.model.journey.{Journey, JourneySpecificData, Url}
import payapi.corcommon.model.{AmountInPence, PaymentStatus, PaymentStatuses}
import payapi.corcommon.model.barclays.{CardCategories, TransactionReference}

import java.time.LocalDateTime

object TestDataUtils {
  //reusable orders
  val debitCardOrder: Option[BarclaysOrder] = Some(BarclaysOrder(
    transactionReference = TransactionReference("Some-transaction-ref"),
    iFrameUrl            = Url("some-url"),
    cardCategory         = Some(CardCategories.debit),
    commissionInPence    = None,
    paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
  ))

  val creditCardOrder: Option[BarclaysOrder] = Some(BarclaysOrder(
    transactionReference = TransactionReference("Some-transaction-ref"),
    iFrameUrl            = Url("some-url"),
    cardCategory         = Some(CardCategories.credit),
    commissionInPence    = Some(AmountInPence(123)),
    paidOn               = Some(LocalDateTime.parse("2027-11-02T16:28:55.185"))
  ))

  val sentOrder: Option[BarclaysOrder] = Some(BarclaysOrder(
    transactionReference = TransactionReference("Some-transaction-ref"),
    iFrameUrl            = Url("some-url"),
    cardCategory         = None,
    commissionInPence    = None,
    paidOn               = None
  ))

  //todo update, I think there's one extra field that needs setting
  val failedOrder: Option[BarclaysOrder] = Some(BarclaysOrder(
    transactionReference = TransactionReference("Some-transaction-ref"),
    iFrameUrl            = Url("some-url"),
    cardCategory         = None,
    commissionInPence    = None,
    paidOn               = None
  ))

  def intoSentWithOrder[Jsd <: JourneySpecificData]: (Journey[Jsd], Option[BarclaysOrder]) => Journey[Jsd] = {
    case (journey, maybeOrder) => intoState(journey, maybeOrder, PaymentStatuses.Sent)
  }

  def intoSuccessWithOrder[Jsd <: JourneySpecificData]: (Journey[Jsd], Option[BarclaysOrder]) => Journey[Jsd] = {
    case (journey, maybeOrder) => intoState(journey, maybeOrder, PaymentStatuses.Successful)
  }

  def intoFailed[Jsd <: JourneySpecificData]: (Journey[Jsd], Option[BarclaysOrder]) => Journey[Jsd] = {
    case (journey, maybeOrder) => intoState(journey, maybeOrder, PaymentStatuses.Failed)
  }

  def intoCancelled[Jsd <: JourneySpecificData]: (Journey[Jsd], Option[BarclaysOrder]) => Journey[Jsd] = {
    case (journey, maybeOrder) => intoState(journey, maybeOrder, PaymentStatuses.Cancelled)
  }

  private def intoState[Jsd <: JourneySpecificData]: (Journey[Jsd], Option[BarclaysOrder], PaymentStatus) => Journey[Jsd] = {
    case (journey, maybeOrder, paymentStatus) => journey.copy(order  = maybeOrder, status = paymentStatus)
  }
}
