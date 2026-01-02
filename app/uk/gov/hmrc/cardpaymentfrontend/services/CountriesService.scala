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

package uk.gov.hmrc.cardpaymentfrontend.services

import uk.gov.hmrc.cardpaymentfrontend.models.Country
import play.Environment
import play.api.i18n._
import play.api.mvc.Request
import uk.gov.hmrc.cardpaymentfrontend.requests.RequestSupport
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps

import javax.inject.{Inject, Singleton}
import scala.io.Source

@Singleton
class CountriesService @Inject() (requestSupport: RequestSupport, env: Environment)(implicit messagesApi: MessagesApi) {

  import requestSupport._

  private val pattern = "([A-Z]{3})=(.*)".r

  private val countriesList: Seq[Country] = countrySeqFromFile("conf/country-codes-en.properties")

  private val countriesListWelsh: Seq[Country] = countrySeqFromFile("conf/country-codes-cy.properties")

  def getCountries(implicit request: Request[_]): Seq[Country] = {

    val UK: (Country) => Boolean = c => c.code === "GBR"

    val countries = request.lang.code match {
      case "en" => countriesList
      case "cy" => countriesListWelsh
      case _    => throw new RuntimeException("Unknown Language from request, cannot derive country list")
    }
    countries.filter(UK) ++ countries.filterNot(UK).sortWith((x, y) => x.name.compareTo(y.name) < 0)

  }

  private def countrySeqFromFile(filename: String): Seq[Country] = {

    val sourceFromFile = Source.fromFile(env.getFile(filename))

    val countries = sourceFromFile
      .getLines()
      .map {
        case pattern(code, name) => Country(name = name, code = code)
        case _                   => throw new RuntimeException("Failed to read country codes from environment file")
      }
      .toSeq

    sourceFromFile.close()
    countries

  }
}
