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

import payapi.corcommon.model.Origins.{BcPngr, Mib}
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.cardpaymentfrontend.controllers.routes

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class JourneyRoutingActionRefiner @Inject() (implicit ec: ExecutionContext) extends ActionRefiner[JourneyRequest, JourneyRequest] with Logging {

  /** Used to determine the first page an origin should land on. Most origins go straight to card fees page, but Mib and BcPngr origins skip that page (and the
    * email page) to go straight to address page.
    */

  override protected[actions] def refine[A](request: JourneyRequest[A]): Future[Either[Result, JourneyRequest[A]]] = {
    request.journey.origin match {
      case Mib | BcPngr => Future.successful(Left(Redirect(routes.AddressController.renderPage)))
      case _            => Future.successful(Right(request))
    }
  }

  override protected def executionContext: ExecutionContext = ec
}
