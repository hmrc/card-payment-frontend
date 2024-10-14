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

package uk.gov.hmrc.cardpaymentfrontend.actions

import play.api.mvc.{ActionRefiner, Request, Result, Results}
import uk.gov.hmrc.cardpaymentfrontend.connectors.PayApiConnector
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class GetJourneyActionRefiner @Inject() (
    payApiConnector: PayApiConnector,
    requestSupport:  RequestSupport
)(implicit ec: ExecutionContext) extends ActionRefiner[Request, JourneyRequest] {

  override protected[actions] def refine[A](request: Request[A]): Future[Either[Result, JourneyRequest[A]]] = {

    implicit val r: Request[A] = request

    payApiConnector.findLatestJourneyBySessionId()(requestSupport.hc)
      .map {
        case Some(journey) => Right(new JourneyRequest(journey, request))
        case None          => Left(Results.Unauthorized("need a session id")) //should probably be a redirect to pay-frontend /pay
      }
  }

  override protected def executionContext: ExecutionContext = ec
}
