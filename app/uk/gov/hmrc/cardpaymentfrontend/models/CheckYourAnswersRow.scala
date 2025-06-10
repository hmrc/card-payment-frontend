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

import payapi.corcommon.model.taxes.epaye.SubYearlyEpayeTaxPeriod
import play.api.i18n.Messages
import uk.gov.hmrc.cardpaymentfrontend.util.Period.humanReadablePeriod
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryListRow, Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions}

abstract class AnswersRow

final case class CheckYourAnswersRow(titleMessageKey: String, value: Seq[String], changeLink: Option[Link]) extends AnswersRow
final case class CheckYourAnswersPeriodRow(titleMessageKey: String, value: Seq[Option[SubYearlyEpayeTaxPeriod]], changeLink: Option[Link]) extends AnswersRow

object CheckYourAnswersRow {

  def summarise(answerRow: AnswersRow)(implicit messages: Messages): SummaryListRow = {

    answerRow match {
      case checkYourAnswersPeriodRow: CheckYourAnswersPeriodRow =>
        checkYourAnswersPeriodRow.value match {
          case seqOfPeriods if seqOfPeriods.nonEmpty =>
            summaryListRow(checkYourAnswersPeriodRow.titleMessageKey, seqOfPeriods.flatMap(_.map(humanReadablePeriod(_, messages.lang))), checkYourAnswersPeriodRow.changeLink)
          case _ => summaryListWithoutValue(checkYourAnswersPeriodRow.titleMessageKey, checkYourAnswersPeriodRow.changeLink)
        }
      case checkYourAnswerRow: CheckYourAnswersRow =>
        checkYourAnswerRow.value match {
          case seqOfString if seqOfString.nonEmpty => summaryListRow(checkYourAnswerRow.titleMessageKey, seqOfString, checkYourAnswerRow.changeLink)
          case _                                   => summaryListWithoutValue(checkYourAnswerRow.titleMessageKey, checkYourAnswerRow.changeLink)
        }
      case _ => throw new IllegalArgumentException("Unknown AnswersRow type")
    }
  }

  private def summaryListRow(titleMessageKey: String, seqOfString: Seq[String], changeLink: Option[Link])(implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key     = Key(content = Text(Messages(titleMessageKey))),
      value   = Value(content = HtmlContent(Messages(seqOfString.mkString("</br>")))),
      actions = changeLink match {
        case Some(link) => Some(
          Actions(items = Seq(ActionItem(
            href               = link.href.url,
            content            = Text(Messages(link.messageKey)),
            visuallyHiddenText = link.visuallyHiddenMessageKey,
            attributes         = Map(
              "id" -> link.linkId
            )
          )))
        )
        case None => None
      }
    )
  }

  private def summaryListWithoutValue(titleMessageKey: String, changeLink: Option[Link])(implicit messages: Messages): SummaryListRow = {
    SummaryListRow(
      key   = Key(content = Text(Messages(titleMessageKey))),
      value = changeLink match {
        case Some(link) => Value(HtmlContent(s"""<a id="${link.linkId}" href="${link.href.url}" class="govuk-link">${messages(link.messageKey)}</a>"""))
        case None       => Value()
      }
    )
  }
}
