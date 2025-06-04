package uk.gov.hmrc.cardpaymentfrontend.models.extendedorigins

import payapi.cardpaymentjourney.model.journey.{JourneySpecificData, JsdPfEpayeSeta}
import play.api.mvc.AnyContent
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.models.PaymentMethod.OneOffDirectDebit
import uk.gov.hmrc.cardpaymentfrontend.models.{CheckYourAnswersRow, PaymentMethod}
import uk.gov.hmrc.cardpaymentfrontend.models.openbanking.{OriginSpecificSessionData, PfEpayeSetaSessionData}

object ExtendedPfEpayeSeta extends ExtendedOrigin {

  override def serviceNameMessageKey: String = "service-name.PfEpayeSeta"

  override def taxNameMessageKey: String = "payment-complete.tax-name.PfEpayeSeta"

  override def cardFeesPagePaymentMethods: Set[PaymentMethod] = Set()

  override def paymentMethods(): Set[PaymentMethod] = Set(OneOffDirectDebit)

  override def checkYourAnswersReferenceRow(journeyRequest: JourneyRequest[AnyContent])
                                           (payFrontendBaseUrl: String): Option[CheckYourAnswersRow] =
    Some(CheckYourAnswersRow(
      titleMessageKey = "check-your-details.PfEpayeSeta.reference",
      value           = Seq(journeyRequest.journey.referenceValue),
      changeLink      = None // TODO: is this true?
    ))

  override def openBankingOriginSpecificSessionData: JourneySpecificData => Option[OriginSpecificSessionData] = {
    case j: JsdPfEpayeSeta => j.psaNumber.map(PfEpayeSetaSessionData(_))
    case _ => throw new RuntimeException("Incorrect origin found")
  }

  override def emailTaxTypeMessageKey: String = "email.tax-name.PfEpayeSeta"

  override def surveyAuditName: String = "paye-seta"

  override def surveyReturnHref: String = "https://www.gov.uk/government/organisations/hm-revenue-customs"

  override def surveyReturnMessageKey: String = "payments-survey.other.return-message"

  override def surveyIsWelshSupported: Boolean = true

  override def surveyBannerTitle: String = serviceNameMessageKey
}
