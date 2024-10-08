import sbt.*

//format: OFF
object AppDependencies {

  val payApiCorVersion = "1.237.0"
  private val bootstrapVersion = "9.5.0"

  val compile: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "10.13.0",
    "uk.gov.hmrc" %% "pay-api-cor-card-payment-journey" % payApiCorVersion,
  )

  val test: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"   %  "jsoup"                  % "1.18.1"
  ).map( _ % Test )

  val it: Seq[ModuleID] = Seq.empty[ModuleID]
}
