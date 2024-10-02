import uk.gov.hmrc.DefaultBuildSettings

//--- defining here so it can be set before running sbt like `sbt 'set Global / strictBuilding := true' ...`
val strictBuilding: SettingKey[Boolean] = StrictBuilding.strictBuilding
StrictBuilding.strictBuildingSetting
//---

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "2.13.12"

lazy val microservice = Project("card-payment-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    scalacOptions ++= ScalaCompilerFlags.scalaCompilerOptions,
    scalacOptions ++= {
      if (StrictBuilding.strictBuilding.value) ScalaCompilerFlags.strictScalaCompilerOptions else Nil
    },
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    pipelineStages := Seq(gzip),
  )
  .settings(PlayKeys.playDefaultPort := 10155)
  .settings(commands ++= SbtCommands.commands)
  .settings(CodeCoverageSettings.settings *)
  .settings(SbtUpdatesSettings.sbtUpdatesSettings *)
  .settings(ScalariformSettings.scalariformSettings *)
  .settings(WartRemoverSettings.wartRemoverSettings: _*)
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
    Compile / scalacOptions -= "utf8",
  )

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
