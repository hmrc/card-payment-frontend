import sbt.*

//format: OFF
object AppDependencies {

  private val payApiCorVersion = "1.332.0"
  private val bootstrapVersion = "10.9.0"
  private val enumeratumVersion = "1.9.9"

  val compile: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc"  %% "bootstrap-frontend-play-30"       % bootstrapVersion,
    "uk.gov.hmrc"  %% "play-frontend-hmrc-play-30"       % "13.18.0",
    "com.beachape" %% "enumeratum"                       % enumeratumVersion,
    "com.beachape" %% "enumeratum-play"                  % enumeratumVersion,
    "uk.gov.hmrc"  %% "pay-api-cor-card-payment-journey" % payApiCorVersion
  )

  val test: Seq[ModuleID] = Seq[ModuleID](
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"   %  "jsoup"                  % "1.24.1"
  ).map( _ % Test )

  val it: Seq[ModuleID] = Seq.empty[ModuleID]
}
