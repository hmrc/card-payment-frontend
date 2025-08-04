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

package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.JourneySpecificData
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.OriginSpecificSessionData

object ExtendedPfJobRetentionScheme extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfJobRetentionScheme"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfJobRetentionScheme"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OneOffDirectDebit)
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OneOffDirectDebit, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfJobRetentionScheme.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = Some(Link(
        href       = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),
        linkId     = "check-your-details-reference-change-link",
        messageKey = "check-your-details.change"
      ))
    ))
  }

  override def surveyAuditName: String = "job-retention-scheme"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = true
  override def surveyBannerTitle: String = serviceNameMessageKey

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfJobRetentionScheme"

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = _ => None

}
