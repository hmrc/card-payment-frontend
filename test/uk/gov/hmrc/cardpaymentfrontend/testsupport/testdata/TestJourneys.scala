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

import payapi.cardpaymentjourney.model.journey.{Journey, JsdPfSa, SessionId}
import payapi.corcommon.model.{JourneyId, PaymentStatuses}

import java.time.LocalDateTime

object TestJourneys {

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
}
