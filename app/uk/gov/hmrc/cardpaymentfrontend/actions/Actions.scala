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

package uk.gov.hmrc.cardpaymentfrontend.actions

import play.api.mvc.{ActionBuilder, AnyContent, DefaultActionBuilder, Request}

import javax.inject.{Inject, Singleton}

@Singleton
class Actions @Inject() (
    actionBuilder:                DefaultActionBuilder,
    getJourneyActionRefiner:      GetJourneyActionRefiner,
    journeyFinishedActionRefiner: JourneyFinishedActionRefiner,
    paymentStatusActionRefiners:  PaymentStatusActionRefiners
) {

  val default: ActionBuilder[Request, AnyContent] = actionBuilder

  val journeyAction: ActionBuilder[JourneyRequest, AnyContent] = default.andThen[JourneyRequest](getJourneyActionRefiner)

  val iframeAction: ActionBuilder[JourneyRequest, AnyContent] = journeyAction.andThen[JourneyRequest](paymentStatusActionRefiners.iframePageActionRefiner)

  val paymentStatusAction: ActionBuilder[JourneyRequest, AnyContent] = journeyAction.andThen[JourneyRequest](paymentStatusActionRefiners.paymentStatusActionRefiner)

  val journeyFinishedAction: ActionBuilder[JourneyRequest, AnyContent] = journeyAction.andThen[JourneyRequest](journeyFinishedActionRefiner)

}
