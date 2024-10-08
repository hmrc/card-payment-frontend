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

package uk.gov.hmrc.cardpaymentfrontend.controllers

import payapi.corcommon.model.Origins
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import uk.gov.hmrc.cardpaymentfrontend.utils.OriginExtraInfo
import uk.gov.hmrc.cardpaymentfrontend.views.html.FeesPage
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject

class FeesController @Inject() (
    originExtraInfo: OriginExtraInfo,
    mcc:             MessagesControllerComponents,
    feesPage:        FeesPage
) extends FrontendController(mcc) {
  //A base case
  def renderPage0(): Action[AnyContent] = Action { implicit request =>
    Ok(feesPage(
      originExtraInfo.paymentMethod(Origins.PfOther),
      ("bank account", Call("GET", "http://SomeURL")),
      None,
      None
    ))
  }

  //Show Open Banking style link
  def renderPage1(): Action[AnyContent] = Action { implicit request =>
    println(s"====== originExtraInfo.paymentMethod(Origins.PfVat): ${originExtraInfo.paymentMethod(Origins.PfVat).toString()}")
    Ok(feesPage(
      originExtraInfo.paymentMethod(Origins.PfVat),
      ("bank account", Call("GET", "http://SomeURL")),
      Some(("direct debit", Call("GET", "http://SomeDDURL"))),
      None
    ))
  }

  //Show Variable Direct Debit link
  def renderPage2(): Action[AnyContent] = Action { implicit request =>
    Ok(feesPage(
      originExtraInfo.paymentMethod(Origins.PfSdil),
      ("bank account", Call("GET", "http://SomeURL")),
      Some(("direct debit", Call("GET", "http://SomeDDURL"))),
      None
    ))
  }

  //Show one off direct debit link
  def renderPage3(): Action[AnyContent] = Action { implicit request =>
    Ok(feesPage(
      originExtraInfo.paymentMethod(Origins.PfTpes),
      ("bank account", Call("GET", "http://SomeURL")),
      Some(("direct debit", Call("GET", "http://SomeDDURL"))),
      None
    ))
  }

  def renderPage4(): Action[AnyContent] = Action { implicit request =>
    Ok(feesPage(
      originExtraInfo.paymentMethod(Origins.BtaEpayeBill),
      ("bank account", Call("GET", "http://SomeURL")),
      Some(("direct debit", Call("GET", "http://SomeDDURL"))),
      None
    ))
  }

  def renderPage5(): Action[AnyContent] = Action { implicit request =>
    Ok(feesPage(
      originExtraInfo.paymentMethod(Origins.BtaEpayeBill),
      ("bank account", Call("GET", "http://SomeURL")),
      Some(("direct debit", Call("GET", "http://SomeDDURL"))),
      Some(("alt direct debit", Call("GET", "http://SomeDDURL")))
    ))
  }

}
