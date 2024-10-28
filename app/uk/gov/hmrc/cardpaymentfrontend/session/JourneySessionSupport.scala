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

package uk.gov.hmrc.cardpaymentfrontend.session

import payapi.corcommon.model.JourneyId
import play.api.libs.json.{Format, JsObject, Json}
import play.api.mvc._

object JourneySessionSupport extends JourneySessionSupport

/**
 * Add/Remove/Replace journey related data to Session. The data are placed under JourneyId key.
 * The data are serialised as Json.
 *
 * See the spec
 */
trait JourneySessionSupport {

  object Keys {
    val address = "address"
    val email = "email"
  }

  implicit class ResultOps(r: Result)(implicit request: RequestHeader) {

    def placeInSession[T: Format](journeyId: JourneyId, data: (String, T)*): Result = {
      val newSession = data.foldLeft[Session](r.session) {
        case (session, (key, value)) =>
          add(key, value, session, journeyId)
      }
      r.withSession(newSession)
    }

    def removingFromSession(journeyId: JourneyId, keys: String*): Result = {
      val newSession = keys.foldLeft[Session](r.session) { (session, key) =>
        remove(key, session, journeyId)
      }
      r.withSession(newSession)
    }

    def removeAllFromSession(journeyId: JourneyId): Result = {
      r.withSession(r.session - journeyId.value)
    }

    def readFromSession[T: Format](journeyId: JourneyId, key: String): Option[T] =
      r.session
        .get(journeyId.value)
        .map(allJourneyData => Json.parse(allJourneyData))
        .flatMap(json => (json \ key).toOption)
        .map(_.as[T])
  }

  implicit class RequestOps(r: Request[_]) {

    def readFromSession[T: Format](journeyId: JourneyId, key: String): Option[T] = r
      .session
      .get(journeyId.value)
      .map { allJourneyData =>
        println("inside read from session")
        println(allJourneyData)
        Json.parse(allJourneyData)
      }
      .flatMap(json => (json \ key).toOption)
      .map(_.as[T])
  }

  /**
   * Adds (replacing existing) values to session in the journey scope.
   * Technically speaking it creates (if not present) empty json object under journeyId key and adds data to it.
   */
  private def add[T: Format](key: String, data: T, session: Session, journeyId: JourneyId): Session = {
    val current: JsObject = session
      .get(journeyId.value)
      .map(jsonString => Json.parse(jsonString).as[JsObject])
      .getOrElse(Json.obj())

    val addon = Json.obj(key -> data)
    val newSessionEntry = current ++ addon
    session + (journeyId.value -> Json.prettyPrint(newSessionEntry))
  }

  private def remove(key: String, session: Session, journeyId: JourneyId): Session = {
    val current: JsObject = session
      .get(journeyId.value)
      .map(jsonString => Json.parse(jsonString).as[JsObject])
      .getOrElse(Json.obj())

    val newSessionEntry = current - key
    session + (journeyId.value -> Json.prettyPrint(newSessionEntry))
  }

}
