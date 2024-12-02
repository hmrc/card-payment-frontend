import sbt.*

//format: OFF
object AppDependencies {

  val payApiCorVersion = "1.243.0"
  private val bootstrapVersion = "9.5.0"

  private val payApiExclusionRules: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "uk.gov.hmrc", name = "bootstrap-backend-play-29_2.13"),
    ExclusionRule(organization = "uk.gov.hmrc", name = "reference-checker_2.13"),
    ExclusionRule(organization = "uk.gov.hmrc.mongo", name = "hmrc-mongo-play-29_2.13"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30"       % "11.6.0",
    "uk.gov.hmrc" %% "pay-api-cor-card-payment-journey" % payApiCorVersion excludeAll(payApiExclusionRules: _*)
  )

  val test: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"   %  "jsoup"                  % "1.18.3"
  ).map( _ % Test )

  val it: Seq[ModuleID] = Seq.empty[ModuleID]
}
