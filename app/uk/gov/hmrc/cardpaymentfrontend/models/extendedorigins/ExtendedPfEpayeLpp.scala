package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfEpayeLpp}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.{Card, OneOffDirectDebit, OpenBanking}
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfEpayeLppSessionData}

object ExtendedPfEpayeLpp extends ExtendedOrigin {

  override def serviceNameMessageKey: String = "service-name.PfEpayeLpp"

  override def taxNameMessageKey: String = "payment-complete.tax-name.PfEpayeLpp"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set(OneOffDirectDebit, OpenBanking)

  override def paymentMethods(): Set[PaymentMethod] = Set(OneOffDirectDebit, Card, OpenBanking)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
                                           (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfEpayeLpp.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None
    ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfEpayeLpp => j.prn.map(PfEpayeLppSessionData(_))
    case _ => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfEpayeLpp"

  override def surveyAuditName: String = "paye-lpp"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
