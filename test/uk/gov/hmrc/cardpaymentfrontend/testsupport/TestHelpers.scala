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

package uk.gov.hmrc.cardpaymentfrontend.testsupport

import org.scalatest.AppendedClues.convertToClueful
import payapi.cardpaymentjourney.model.journey._
import payapi.corcommon.model.Origins._
import payapi.corcommon.model.{Origin, Origins}
import uk.gov.hmrc.cardpaymentfrontend.testsupport.testdata.{JourneyStatuses, TestJourneys}

class TestHelpers extends UnitSpec {
  "all origins should be covered in implemented and unimplemented origins" in {
    TestHelpers.implementedOrigins ++ TestHelpers.unimplementedOrigins should contain theSameElementsAs Origins.values withClue s"\n\n *** Missing origin from one of the lists: [ ${Origins.values.diff(TestHelpers.implementedOrigins ++ TestHelpers.unimplementedOrigins).mkString(", ")} ]\n\n"
  }
}

object TestHelpers {

  val implementedOrigins: Seq[Origin] = Seq[Origin](
    PfSa,
    BtaSa,
    PtaSa,
    ItSa,
    PfAlcoholDuty,
    AlcoholDuty,
    PfCt,
    BtaCt,
    PfEpayeNi,
    PfEpayeLpp,
    PfEpayeLateCis,
    PfEpayeP11d,
    PfEpayeSeta,
    PfVat,
    BtaVat,
    VcVatReturn,
    VcVatOther,
    Ppt,
    PfPpt,
    BtaEpayeBill,
    BtaEpayePenalty,
    BtaEpayeInterest,
    BtaEpayeGeneral,
    BtaClass1aNi,
    PfAmls,
    Amls
  )

  val unimplementedOrigins: Seq[Origin] = Seq[Origin](
    PfSdlt,
    PfCds,
    PfOther,
    PfP800,
    PtaP800,
    PfClass2Ni,
    PfInsurancePremium,
    PfPsAdmin,
    AppSa,
    BtaSdil,
    BcPngr,
    Parcels,
    DdVat,
    DdSdil,
    PfCdsCash,
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
    PfAted,
    PfCdsDeferment,
    PfTrust,
    PtaClass3Ni,
    VatC2c,
    `3psSa`,
    `3psVat`,
    PfPillar2,
    PfVatC2c,
    Pillar2
  )

  def deriveTestDataFromOrigin[jsd <: JourneySpecificData](origin: Origin): JourneyStatuses[_ >: JsdPfSa with JsdBtaSa with JsdPtaSa with JsdItSa with JsdPfVat with JsdPfCt with JsdPfEpayeNi with JsdPfEpayeLpp with JsdPfEpayeSeta with JsdPfEpayeLateCis with JsdPfEpayeP11d with JsdBtaVat with JsdBtaCt with JsdVcVatReturn with JsdVcVatOther with JsdAlcoholDuty with JsdPfAlcoholDuty with JsdBtaEpayeBill with JsdBtaEpayePenalty with JsdBtaEpayeGeneral with JsdBtaEpayeInterest with JsdBtaClass1aNi with JsdPpt with JsdPfPpt with JsdPfAmls with JsdAmls <: JourneySpecificData] = origin match {
    case Origins.PfSa                     => TestJourneys.PfSa
    case Origins.BtaSa                    => TestJourneys.BtaSa
    case Origins.PtaSa                    => TestJourneys.PtaSa
    case Origins.ItSa                     => TestJourneys.ItSa
    case Origins.PfVat                    => TestJourneys.PfVat
    case Origins.PfCt                     => TestJourneys.PfCt
    case Origins.PfEpayeNi                => TestJourneys.PfEpayeNi
    case Origins.PfEpayeLpp               => TestJourneys.PfEpayeLpp
    case Origins.PfEpayeSeta              => TestJourneys.PfEpayeSeta
    case Origins.PfEpayeLateCis           => TestJourneys.PfEpayeLateCis
    case Origins.PfEpayeP11d              => TestJourneys.PfEpayeP11d
    case Origins.PfSdlt                   => throw new MatchError("Not implemented yet")
    case Origins.PfCds                    => throw new MatchError("Not implemented yet")
    case Origins.PfOther                  => throw new MatchError("Not implemented yet")
    case Origins.PfP800                   => throw new MatchError("Not implemented yet")
    case Origins.PtaP800                  => throw new MatchError("Not implemented yet")
    case Origins.PfClass2Ni               => throw new MatchError("Not implemented yet")
    case Origins.PfInsurancePremium       => throw new MatchError("Not implemented yet")
    case Origins.PfPsAdmin                => throw new MatchError("Not implemented yet")
    case Origins.AppSa                    => throw new MatchError("Not implemented yet")
    case Origins.BtaVat                   => TestJourneys.BtaVat
    case Origins.BtaEpayeBill             => TestJourneys.BtaEpayeBill
    case Origins.BtaEpayePenalty          => TestJourneys.BtaEpayePenalty
    case Origins.BtaEpayeInterest         => TestJourneys.BtaEpayeInterest
    case Origins.BtaEpayeGeneral          => TestJourneys.BtaEpayeGeneral
    case Origins.BtaClass1aNi             => TestJourneys.BtaClass1aNi
    case Origins.BtaCt                    => TestJourneys.BtaCt
    case Origins.BtaSdil                  => throw new MatchError("Not implemented yet")
    case Origins.BcPngr                   => throw new MatchError("Not implemented yet")
    case Origins.Parcels                  => throw new MatchError("Not implemented yet")
    case Origins.DdVat                    => throw new MatchError("Not implemented yet")
    case Origins.DdSdil                   => throw new MatchError("Not implemented yet")
    case Origins.VcVatReturn              => TestJourneys.VcVatReturn
    case Origins.VcVatOther               => TestJourneys.VcVatOther
    case Origins.Amls                     => TestJourneys.Amls
    case Origins.Ppt                      => TestJourneys.Ppt
    case Origins.PfCdsCash                => throw new MatchError("Not implemented yet")
    case Origins.PfPpt                    => TestJourneys.PfPpt
    case Origins.PfSpiritDrinks           => throw new MatchError("Not implemented yet")
    case Origins.PfInheritanceTax         => throw new MatchError("Not implemented yet")
    case Origins.Mib                      => throw new MatchError("Not implemented yet")
    case Origins.PfClass3Ni               => throw new MatchError("Not implemented yet")
    case Origins.PfWineAndCider           => throw new MatchError("Not implemented yet")
    case Origins.PfBioFuels               => throw new MatchError("Not implemented yet")
    case Origins.PfAirPass                => throw new MatchError("Not implemented yet")
    case Origins.PfMgd                    => throw new MatchError("Not implemented yet")
    case Origins.PfBeerDuty               => throw new MatchError("Not implemented yet")
    case Origins.PfGamingOrBingoDuty      => throw new MatchError("Not implemented yet")
    case Origins.PfGbPbRgDuty             => throw new MatchError("Not implemented yet")
    case Origins.PfLandfillTax            => throw new MatchError("Not implemented yet")
    case Origins.PfSdil                   => throw new MatchError("Not implemented yet")
    case Origins.PfAggregatesLevy         => throw new MatchError("Not implemented yet")
    case Origins.PfClimateChangeLevy      => throw new MatchError("Not implemented yet")
    case Origins.PfSimpleAssessment       => throw new MatchError("Not implemented yet")
    case Origins.PtaSimpleAssessment      => throw new MatchError("Not implemented yet")
    case Origins.AppSimpleAssessment      => throw new MatchError("Not implemented yet")
    case Origins.PfTpes                   => throw new MatchError("Not implemented yet")
    case Origins.CapitalGainsTax          => throw new MatchError("Not implemented yet")
    case Origins.EconomicCrimeLevy        => throw new MatchError("Not implemented yet")
    case Origins.PfEconomicCrimeLevy      => throw new MatchError("Not implemented yet")
    case Origins.PfJobRetentionScheme     => throw new MatchError("Not implemented yet")
    case Origins.JrsJobRetentionScheme    => throw new MatchError("Not implemented yet")
    case Origins.PfImportedVehicles       => throw new MatchError("Not implemented yet")
    case Origins.PfChildBenefitRepayments => throw new MatchError("Not implemented yet")
    case Origins.NiEuVatOss               => throw new MatchError("Not implemented yet")
    case Origins.PfNiEuVatOss             => throw new MatchError("Not implemented yet")
    case Origins.NiEuVatIoss              => throw new MatchError("Not implemented yet")
    case Origins.PfNiEuVatIoss            => throw new MatchError("Not implemented yet")
    case Origins.PfAmls                   => TestJourneys.PfAmls
    case Origins.PfAted                   => throw new MatchError("Not implemented yet")
    case Origins.PfCdsDeferment           => throw new MatchError("Not implemented yet")
    case Origins.PfTrust                  => throw new MatchError("Not implemented yet")
    case Origins.PtaClass3Ni              => throw new MatchError("Not implemented yet")
    case Origins.AlcoholDuty              => TestJourneys.AlcoholDuty
    case Origins.PfAlcoholDuty            => TestJourneys.PfAlcoholDuty
    case Origins.VatC2c                   => throw new MatchError("Not implemented yet")
    case Origins.`3psSa`                  => throw new MatchError("Not implemented yet")
    case Origins.`3psVat`                 => throw new MatchError("Not implemented yet")
    case Origins.PfPillar2                => throw new MatchError("Not implemented yet")
    case Origins.PfVatC2c                 => throw new MatchError("Not implemented yet")
    case Origins.Pillar2                  => throw new MatchError("Not implemented yet")
  }

}
