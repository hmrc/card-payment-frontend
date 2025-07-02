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

package uk.gov.hmrc.cardpaymentfrontend.testsupport.mockclasses

import payapi.corcommon.model.{Origin, OriginCode, TransNumberGenerator}

class MockTransNumberGenerator extends TransNumberGenerator {
  //create this instead of the random 9 digits at the end.
  override def generate(origin: Origin): String = s"000${OriginCode.codeForOrigin(origin)}999999999"
}
