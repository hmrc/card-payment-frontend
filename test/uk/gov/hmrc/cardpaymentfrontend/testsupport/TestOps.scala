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

package uk.gov.hmrc.cardpaymentfrontend.testsupport

import play.api.mvc.Cookie
import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

object TestOps {
  implicit class FakeRequestOps[T](r: FakeRequest[T]) {
    def withLang(language: String = "en"): FakeRequest[T] = r.withCookies(Cookie("PLAY_LANG", language))

    def withLangWelsh(): FakeRequest[T] = r.withLang("cy")
    def withLangEnglish(): FakeRequest[T] = r.withLang("en")

    def withSessionId(sessionId: String = "some-valid-session-id"): FakeRequest[T] =
      r.withSession(SessionKeys.sessionId -> sessionId)
  }
}
