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

package uk.gov.hmrc.cardpaymentfrontend.models.email

import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.cardpaymentfrontend.models.EmailAddress

final case class EmailRequest(
    to:         List[EmailAddress],
    templateId: String,
    parameters: EmailParameters,
    force:      Boolean
)

@SuppressWarnings(Array("org.wartremover.warts.Any"))
object EmailRequest {
  implicit val format: Format[EmailRequest] = Json.format[EmailRequest]
}
