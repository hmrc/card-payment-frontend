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
  actionFilters:                ActionFilters,
  getJourneyActionRefiner:      GetJourneyActionRefiner,
  journeyFinishedActionRefiner: JourneyFinishedActionRefiner,
  journeyRoutingActionRefiner:  JourneyRoutingActionRefiner,
  paymentStatusActionRefiners:  PaymentStatusActionRefiners
) {

  val default: ActionBuilder[Request, AnyContent] = actionBuilder.andThen[Request](actionFilters.defaultKibanaLoggingFilter)

  val journeyAction: ActionBuilder[JourneyRequest, AnyContent] =
    actionBuilder
      .andThen[JourneyRequest](getJourneyActionRefiner)
      .andThen[JourneyRequest](actionFilters.journeyKibanaLoggingFilter)

  val routedJourneyAction: ActionBuilder[JourneyRequest, AnyContent] = journeyAction.andThen[JourneyRequest](journeyRoutingActionRefiner)

  val iframeAction: ActionBuilder[JourneyRequest, AnyContent] = journeyAction.andThen[JourneyRequest](paymentStatusActionRefiners.iframePageActionRefiner)

  def paymentStatusAction(encryptedJourneyId: String): ActionBuilder[JourneyRequest, AnyContent] =
    actionBuilder
      .andThen[JourneyRequest](paymentStatusActionRefiners.findJourneyBySessionIdFallBackToJourneyIdRefiner(encryptedJourneyId))
      .andThen[JourneyRequest](paymentStatusActionRefiners.paymentStatusActionRefiner)
      .andThen[JourneyRequest](actionFilters.journeyKibanaLoggingFilter)

  val journeyFinishedAction: ActionBuilder[JourneyRequest, AnyContent] = journeyAction.andThen[JourneyRequest](journeyFinishedActionRefiner)

}
