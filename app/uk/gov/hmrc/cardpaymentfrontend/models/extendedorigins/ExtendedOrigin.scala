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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.corcommon.model.Reference
import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import play.api.i18n.Messages
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.CheckYourAnswersRow
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData
import uk.gov.hmrc.cardpaymentfrontend.utils.PaymentMethod

trait ExtendedOrigin {
  def serviceNameMessageKey: String
  def taxNameMessageKey: String

  def amount(request: JourneyRequest[AnyContent]): String = request.journey.amountInPence match {
    case Some(amt) => s"£${amt.inPoundsRoundedFormatted}"
    case None      => "???" //todo: logging here
  }

  def reference(request: JourneyRequest[AnyContent]): String = {
    request.journey.journeySpecificData.reference match {
      case Some(Reference(ref)) => ref
      case None                 => "???" //todo:... and log
    }
  }

  //denotes which links/payment methods to show on the card-fees page.
  def cardFeesPagePaymentMethods: Set[PaymentMethod]
  def paymentMethods(): Set[PaymentMethod]
  def checkYourAnswersRows(request: JourneyRequest[AnyContent])(implicit messages: Messages): Seq[CheckYourAnswersRow]
  def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData]

  //email related content
  def emailTaxTypeMessageKey: String

  //payments survey stuff
  def surveyAuditName: String
  def surveyReturnHref: String
  def surveyReturnMessageKey: String
  def surveyIsWelshSupported: Boolean
  def surveyBannerTitle: String

}
