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

package uk.gov.hmrc.cardpaymentfrontend.models

import uk.gov.hmrc.cardpaymentfrontend.models.cardpayment.ClientIds._
import uk.gov.hmrc.cardpaymentfrontend.testsupport.UnitSpec

class ClientIdSpec extends UnitSpec {

  "client id" - {
    "prod values should be correct" in {
      SAEE.prodCode shouldBe "SAEE"
      SAEC.prodCode shouldBe "SAEC"
      VAEE.prodCode shouldBe "VAEE"
      VAEC.prodCode shouldBe "VAEC"
      COEE.prodCode shouldBe "COEE"
      COEC.prodCode shouldBe "COEC"
      PAEE.prodCode shouldBe "PAEE"
      PAEC.prodCode shouldBe "PAEC"
      MIEE.prodCode shouldBe "MIEE"
      MIEC.prodCode shouldBe "MIEC"
      ETEE.prodCode shouldBe "ETEE"
      ETEC.prodCode shouldBe "ETEC"
      SDEE.prodCode shouldBe "SDEE"
      SDEC.prodCode shouldBe "SDEC"
      MBPE.prodCode shouldBe "MBPE"
      CDEE.prodCode shouldBe "CDEE"
      PSEE.prodCode shouldBe "PSEE"
      PSEC.prodCode shouldBe "PSEC"
      CBEE.prodCode shouldBe "CBEE"
      CBEC.prodCode shouldBe "CBEC"
      OSEE.prodCode shouldBe "OSEE"
      PLPE.prodCode shouldBe "PLPE"
      PLPC.prodCode shouldBe "PLPC"
      NICE.prodCode shouldBe "NICE"
      NICC.prodCode shouldBe "NICC"
    }
    "qa values should be correct" in {
      SAEE.qaCode shouldBe "DSEE1"
      SAEC.qaCode shouldBe "DSEC1"
      VAEE.qaCode shouldBe "DVEE1"
      VAEC.qaCode shouldBe "DVEC1"
      COEE.qaCode shouldBe "DCEE1"
      COEC.qaCode shouldBe "DCEC1"
      PAEE.qaCode shouldBe "DPEE1"
      PAEC.qaCode shouldBe "DPEC1"
      MIEE.qaCode shouldBe "DMEE1"
      MIEC.qaCode shouldBe "DMEC1"
      ETEE.qaCode shouldBe "DEEE1"
      ETEC.qaCode shouldBe "DEEC1"
      SDEE.qaCode shouldBe "DDEE1"
      SDEC.qaCode shouldBe "DDEC1"
      MBPE.qaCode shouldBe "MBEE1"
      CDEE.qaCode shouldBe "CDEE1"
      PSEE.qaCode shouldBe "PSEE1"
      PSEC.qaCode shouldBe "PSEC1"
      CBEE.qaCode shouldBe "CBEE1"
      CBEC.qaCode shouldBe "CBEC1"
      OSEE.qaCode shouldBe "OSEE"
      PLPE.qaCode shouldBe "PLPE"
      PLPC.qaCode shouldBe "PLPC"
      NICE.qaCode shouldBe "NICE"
      NICC.qaCode shouldBe "NICC"
    }
  }

}
