import sbt.*

//format: OFF
object AppDependencies {

  private val payApiCorVersion = "1.273.0"
  // Do not change this from 9.14.0 as later versions cause issues with the service.
  private val bootstrapVersion = "9.14.0"

  private val payApiExclusionRules: Seq[InclusionRule] = Seq(
    ExclusionRule(organization = "uk.gov.hmrc", name = "bootstrap-backend-play-29_2.13"),
    ExclusionRule(organization = "uk.gov.hmrc", name = "reference-checker_2.13"),
    ExclusionRule(organization = "uk.gov.hmrc.mongo", name = "hmrc-mongo-play-29_2.13"),
    ExclusionRule(organization = "com.typesafe.play")
  )

  val compile: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc"  %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"  %% "play-frontend-hmrc-play-30"            % "12.17.0",
    "uk.gov.hmrc"  %% "play-conditional-form-mapping-play-29" % "3.3.0",
    "uk.gov.hmrc"  %% "pay-api-cor-card-payment-journey"      % payApiCorVersion excludeAll(payApiExclusionRules *),
    "com.beachape" %% "enumeratum"                            % "1.9.0",
    "com.beachape" %% "enumeratum-play"                       % "1.9.0"
  )

  val test: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"   %  "jsoup"                  % "1.21.2"
  ).map( _ % Test )

  val it: Seq[ModuleID] = Seq.empty[ModuleID]
}
