import com.timushev.sbt.updates.Compat.ModuleFilter
import com.timushev.sbt.updates.UpdatesKeys.dependencyUpdates
import com.timushev.sbt.updates.UpdatesPlugin.autoImport.{dependencyUpdatesFailBuild, dependencyUpdatesFilter, moduleFilterRemoveValue}
import sbt.Keys.*
import sbt.{Def, *}
import xsbti.compile.CompileAnalysis

object SbtUpdatesSettings {

  lazy val sbtUpdatesSettings: Seq[Def.Setting[_ >: Boolean with Task[CompileAnalysis] with ModuleFilter]] = Seq(
    dependencyUpdatesFailBuild := StrictBuilding.strictBuilding.value,
    (Compile / compile) := ((Compile / compile) dependsOn dependencyUpdates).value,
    dependencyUpdatesFilter -= moduleFilter("org.scala-lang"),
    dependencyUpdatesFilter -= moduleFilter("org.playframework"),
    // ignoring pay-api-cor while 3ps being worked on
    dependencyUpdatesFilter -= moduleFilter("uk.gov.hmrc", "pay-api-cor-card-payment-journey"),
    //ignoring bootstrap while we ascertain whether a there's a bug after 9.14.0
    dependencyUpdatesFilter -= moduleFilter("uk.gov.hmrc", "bootstrap-frontend-play-30"),
    dependencyUpdatesFilter -= moduleFilter("uk.gov.hmrc", "bootstrap-test-play-30")
  )

}
