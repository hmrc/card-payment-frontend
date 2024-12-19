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

import org.scalatest.AppendedClues.convertToClueful
import payapi.corcommon.model.Origins._
import payapi.corcommon.model.{Origin, Origins}

class TestHelpers extends UnitSpec {
  "all origins should be covered in implemented and unimplemented origins" in {
    TestHelpers.implementedOrigins ++ TestHelpers.unimplementedOrigins should contain theSameElementsAs Origins.values withClue s"Missing origin from one of the lists: [ ${Origins.values.diff(TestHelpers.implementedOrigins ++ TestHelpers.unimplementedOrigins).mkString(", ")} ]"
  }
}

object TestHelpers {

  val implementedOrigins: Seq[Origin] = Seq[Origin](
    PfSa,
    BtaSa,
    PtaSa,
    ItSa,
    PfAlcoholDuty,
    AlcoholDuty
  )

  val unimplementedOrigins: Seq[Origin] = Seq[Origin](
    PfVat,
    PfCt,
    PfEpayeNi,
    PfEpayeLpp,
    PfEpayeSeta,
    PfEpayeLateCis,
    PfEpayeP11d,
    PfSdlt,
    PfCds,
    PfOther,
    PfP800,
    PtaP800,
    PfClass2Ni,
    PfInsurancePremium,
    PfPsAdmin,
    AppSa,
    BtaVat,
    BtaEpayeBill,
    BtaEpayePenalty,
    BtaEpayeInterest,
    BtaEpayeGeneral,
    BtaClass1aNi,
    BtaCt,
    BtaSdil,
    BcPngr,
    Parcels,
    DdVat,
    DdSdil,
    VcVatReturn,
    VcVatOther,
    Amls,
    Ppt,
    PfCdsCash,
    PfPpt,
    PfSpiritDrinks,
    PfInheritanceTax,
    Mib,
    PfClass3Ni,
    PfWineAndCider,
    PfBioFuels,
    PfAirPass,
    PfMgd,
    PfBeerDuty,
    PfGamingOrBingoDuty,
    PfGbPbRgDuty,
    PfLandfillTax,
    PfSdil,
    PfAggregatesLevy,
    PfClimateChangeLevy,
    PfSimpleAssessment,
    PtaSimpleAssessment,
    AppSimpleAssessment,
    PfTpes,
    CapitalGainsTax,
    EconomicCrimeLevy,
    PfEconomicCrimeLevy,
    PfJobRetentionScheme,
    JrsJobRetentionScheme,
    PfImportedVehicles,
    PfChildBenefitRepayments,
    NiEuVatOss,
    PfNiEuVatOss,
    NiEuVatIoss,
    PfNiEuVatIoss,
    PfAmls,
    PfAted,
    PfCdsDeferment,
    PfTrust,
    PtaClass3Ni,
    VatC2c,
    `3psSa`
  )

}
