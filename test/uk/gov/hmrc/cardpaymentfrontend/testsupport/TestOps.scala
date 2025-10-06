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

package uk.gov.hmrc.cardpaymentfrontend.testsupport

import payapi.corcommon.model.JourneyId
import play.api.libs.json.Json
import play.api.mvc.Cookie
import play.api.test.FakeRequest
import uk.gov.hmrc.cardpaymentfrontend.models.{Address, EmailAddress}
import uk.gov.hmrc.http.SessionKeys

object TestOps {
  implicit class FakeRequestOps[T](r: FakeRequest[T]) {
    def withLang(language: String = "en"): FakeRequest[T] = r.withCookies(Cookie("PLAY_LANG", language))

    def withLangWelsh(): FakeRequest[T] = r.withLang("cy")

    def withLangEnglish(): FakeRequest[T] = r.withLang("en")

    def withSessionId(sessionId: String = "some-valid-session-id"): FakeRequest[T] =
      r.withSession(SessionKeys.sessionId -> sessionId)

    def withAuthSession(authToken: String = "some-valid-auth-token"): FakeRequest[T] =
      r.withSession(SessionKeys.authToken -> authToken)

    def withEmailInSession(journeyId: JourneyId, email: EmailAddress = EmailAddress("blah@blah.com")): FakeRequest[T] =
      r.withSession(journeyId.value -> Json.obj(
        "email" -> email
      ).toString)

    def withAddressInSession(
        journeyId: JourneyId,
        address:   Address   = Address(line1    = "line1", line2 = Some("line2"), city = Some("city"), county = Some("county"), postcode = Some("AA0AA0"), country = "GBR")
    ): FakeRequest[T] =
      r.withSession(journeyId.value -> Json.obj(
        "address" -> address
      ).toString)

    def withEmailAndAddressInSession(
        journeyId:    JourneyId,
        emailAddress: EmailAddress = EmailAddress("blah@blah.com"),
        address:      Address      = Address(line1    = "line1", postcode = Some("AA0AA0"), country = "GBR")
    ): FakeRequest[T] = r.withSession(journeyId.value -> Json.obj(
      "email" -> emailAddress,
      "address" -> address
    ).toString())
  }
}
