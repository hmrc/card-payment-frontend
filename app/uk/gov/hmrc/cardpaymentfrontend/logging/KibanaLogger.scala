/*
 * Copyright 2026 HM Revenue & Customs
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

package uk.gov.hmrc.cardpaymentfrontend.logging

import play.api.Logger
import play.api.mvc.RequestHeader
import uk.gov.hmrc.cardpaymentfrontend.actions.JourneyRequest
import uk.gov.hmrc.cardpaymentfrontend.util.SafeEquals.EqualsOps
import uk.gov.hmrc.play.http.HeaderCarrierConverter

@SuppressWarnings(Array("org.wartremover.warts.Overloading"))
object KibanaLogger {

  private val logger: Logger = Logger("kibana-logger")

  def contextActionLog()(implicit request: RequestHeader): Unit = info("")

  def debug(message: => String)(implicit request: RequestHeader): Unit = logMessage(message, Debug)

  def info(message: => String)(implicit request: RequestHeader): Unit = logMessage(message, Info)

  def warn(message: => String)(implicit request: RequestHeader): Unit = logMessage(message, Warn)

  def error(message: => String)(implicit request: RequestHeader): Unit = logMessage(message, Error)

  def debug(message: => String, ex: Throwable)(implicit request: RequestHeader): Unit = logMessage(message, ex, Debug)

  def info(message: => String, ex: Throwable)(implicit request: RequestHeader): Unit = logMessage(message, ex, Info)

  def warn(message: => String, ex: Throwable)(implicit request: RequestHeader): Unit = logMessage(message, ex, Warn)

  def error(message: => String, ex: Throwable)(implicit request: RequestHeader): Unit = logMessage(message, ex, Error)

  // Log levels used internally by this logger
  private sealed trait LogLevel derives CanEqual
  private case object Debug extends LogLevel
  private case object Info  extends LogLevel
  private case object Warn  extends LogLevel
  private case object Error extends LogLevel

  private def makeRichMessage(message: String)(implicit requestHeader: RequestHeader): String = requestHeader match {
    case request: JourneyRequest[?] =>
      s"[$httpMethodAndPathInformation]" +
        s"[$referer]" +
        s"[$sessionId]" +
        s"[origin: ${request.journey.origin.entryName}]" +
        s"[latestPaymentStatus: ${request.journey.status.entryName}]" +
        s"[$authTokenSet]" +
        s"[$message]"
    case _                          =>
      s"[$httpMethodAndPathInformation]" +
        s"[$referer]" +
        s"[$sessionId]" +
        s"[$authTokenSet]" +
        s"[$message]"
  }

  private def httpMethodAndPathInformation(using requestHeader: RequestHeader): String =
    s"Request: ${requestHeader.method} ${requestHeader.path}"

  private def referer(using requestHeader: RequestHeader) =
    s"Referer: ${requestHeader.headers.headers.find(_._1 === "Referer").map(_._2).getOrElse("NO_REFERER")}"

  private def sessionId(using requestHeader: RequestHeader): String = {
    val sessionId = requestHeader match {
      case request: JourneyRequest[?] => request.journey.sessionId.map(_.value).getOrElse("NO_SESSION_ID")
      case request                    =>
        val hc = HeaderCarrierConverter.fromRequest(request)
        hc.sessionId.map(_.value).getOrElse("NO_SESSION_ID")
    }
    s"sessionId: $sessionId"
  }

  private def authTokenSet(using requestHeader: RequestHeader) = s"authTokenSet: ${requestHeader.session.data.contains("authToken").toString}"

  private def logMessage(message: => String, level: LogLevel)(using request: RequestHeader): Unit = {
    lazy val richMessage = makeRichMessage(message)
    level match {
      case Debug => logger.debug(richMessage)
      case Info  => logger.info(richMessage)
      case Warn  => logger.warn(richMessage)
      case Error => logger.error(richMessage)
    }
  }

  private def logMessage(message: => String, ex: Throwable, level: LogLevel)(implicit request: RequestHeader): Unit = {
    lazy val richMessage = makeRichMessage(message)
    level match {
      case Debug => logger.debug(richMessage, ex)
      case Info  => logger.info(richMessage, ex)
      case Warn  => logger.warn(richMessage, ex)
      case Error => logger.error(richMessage, ex)
    }
  }

}
