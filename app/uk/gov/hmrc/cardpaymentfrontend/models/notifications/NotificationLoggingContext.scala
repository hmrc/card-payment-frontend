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

package uk.gov.hmrc.cardpaymentfrontend.models.notifications

final case class NotificationLoggingContext(notificationName: String, successMessage: String, unexpectedStatusMessage: String, failureMessage: String)

object NotificationLoggingContext {
  val modsNotificationLoggingContext: NotificationLoggingContext = NotificationLoggingContext(
    notificationName        = "MibNotification",
    successMessage          = "Successfully sent notification to Mods via payments-processor",
    unexpectedStatusMessage = "There was an unexpected problem sending notification to Mods via payments-processor, status response returned:",
    failureMessage          = "There was a critical problem sending notification to Mods via payments-processor"
  )

  //  final val cdsNotificationLoggingContext = NotificationLoggingContext(
  //    notificationName        = "CdsNotification",
  //    successMessage          = "???",
  //    unexpectedStatusMessage = "???",
  //    failureMessage          = "???"
  //  )
}
