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

package uk.gov.hmrc.cardpaymentfrontend.models.cardpayment

sealed trait ClientId {
  def prodCode: String

  def qaCode: String
}

object ClientIds {

  case object SAEE extends ClientId {
    val prodCode = "SAEE"
    val qaCode = "DSEE1"
  }

  case object SAEC extends ClientId {
    val prodCode = "SAEC"
    val qaCode = "DSEC1"
  }

  case object VAEE extends ClientId {
    val prodCode = "VAEE"
    val qaCode = "DVEE1"
  }

  case object VAEC extends ClientId {
    val prodCode = "VAEC"
    val qaCode = "DVEC1"
  }

  case object COEE extends ClientId {
    val prodCode = "COEE"
    val qaCode = "DCEE1"
  }

  case object COEC extends ClientId {
    val prodCode = "COEC"
    val qaCode = "DCEC1"
  }

  case object PAEE extends ClientId {
    val prodCode = "PAEE"
    val qaCode = "DPEE1"
  }

  case object PAEC extends ClientId {
    val prodCode = "PAEC"
    val qaCode = "DPEC1"
  }

  case object MIEE extends ClientId {
    val prodCode = "MIEE"
    val qaCode = "DMEE1"
  }

  case object MIEC extends ClientId {
    val prodCode = "MIEC"
    val qaCode = "DMEC1"
  }

  case object ETEE extends ClientId {
    val prodCode = "ETEE"
    val qaCode = "DEEE1"
  }

  case object ETEC extends ClientId {
    val prodCode = "ETEC"
    val qaCode = "DEEC1"
  }

  case object SDEE extends ClientId {
    val prodCode = "SDEE"
    val qaCode = "DDEE1"
  }

  case object SDEC extends ClientId {
    val prodCode = "SDEC"
    val qaCode = "DDEC1"
  }

  case object MBPE extends ClientId {
    val prodCode = "MBPE"
    val qaCode = "MBEE1"
  }

  case object CDEE extends ClientId {
    val prodCode = "CDEE"
    val qaCode = "CDEE1"
  }

  case object PSEE extends ClientId {
    val prodCode = "PSEE"
    val qaCode = "PSEE1"
  }

  case object PSEC extends ClientId {
    val prodCode = "PSEC"
    val qaCode = "PSEC1"
  }

  case object CBEE extends ClientId {
    val prodCode = "CBEE"
    val qaCode = "CBEE1"
  }

  case object CBEC extends ClientId {
    val prodCode = "CBEC"
    val qaCode = "CBEC1"
  }

  case object OSEE extends ClientId {
    val prodCode = "OSEE"
    val qaCode = "OSEE"
  }

  case object PLPE extends ClientId {
    val prodCode = "PLPE"
    val qaCode = "PLPE"
  }

  case object PLPC extends ClientId {
    val prodCode = "PLPC"
    val qaCode = "PLPC"
  }

  case object NICE extends ClientId {
    val prodCode = "NICE"
    val qaCode = "NICE"
  }

  case object NICC extends ClientId {
    val prodCode = "NICC"
    val qaCode = "NICC"
  }

}
