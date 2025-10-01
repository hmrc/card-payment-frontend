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

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfNiEuVatIoss}
import play.api.i18n.Lang
import play.api.mvc.{AnyContent, Call}
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod._
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfNiEuVatIossSessionData}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, Link, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.util.Period.displayCalendarPeriodMonth

object ExtendedPfNiEuVatIoss extends ExtendedOrigin {
  override val serviceNameMessageKey: String = "service-name.PfNiEuVatIoss"
  override val taxNameMessageKey: String = "payment-complete.tax-name.PfNiEuVatIoss"

  def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OpenBanking)
  def paymentMethods(): Set[PaymentMethod] = Set(Card, OpenBanking, Bacs)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])(payFrontendBaseUrl: String): Option[CheckYourAnswersRow] = {
    journeyRequest.journey.journeySpecificData.reference.map { vrn =>
      CheckYourAnswersRow(
        titleMessageKey = "check-your-details.PfNiEuVatIoss.reference",
        value           = Seq(vrn.value),
        changeLink      = Some(Link(
          href       = Call("GET", changeReferenceUrl(payFrontendBaseUrl)),
          linkId     = "check-your-details-reference-change-link",
          messageKey = "check-your-details.change"
        ))
      )
    }
  }

  override def checkYourAnswersAdditionalReferenceRow(journeyRequest: JourneyRequest[AnyContent])
    (payFrontendBaseUrl: String)(implicit lang: Lang): Option[CheckYourAnswersRow] = {
    journeyRequest.journey.journeySpecificData match {
      case jsd: JsdPfNiEuVatIoss =>
        jsd.period.map { period =>
          CheckYourAnswersRow(
            titleMessageKey = "check-your-details.PfNiEuVatIoss.tax-year",
            value           = Seq(displayCalendarPeriodMonth(period)),
            changeLink      = None
          )
        }
      case _ => None
    }
  }

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfNiEuVatIoss =>
      for {
        vrn <- j.ioss
        period <- j.period
      } yield PfNiEuVatIossSessionData(vrn, period)
    case _ =>
      throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfNiEuVatIoss"

  override def surveyAuditName: String = "vat"
  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"
  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"
  override def surveyIsWelshSupported: Boolean = false
  override def surveyBannerTitle: String = serviceNameMessageKey
}
