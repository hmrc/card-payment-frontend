import play.sbt.routes.RoutesKeys.routes
import sbt.Keys.compile
import sbt.{Compile, Test}
import wartremover.WartRemover.autoImport.{wartremoverErrors, wartremoverExcluded}
import wartremover.{Wart, Warts}

object WartRemoverSettings {

  val wartRemoverSettings =
    Seq(
      (Compile / compile / wartremoverErrors) ++= {
        if (StrictBuilding.strictBuilding.value) {
          Warts.allBut(
            Wart.DefaultArguments,
            Wart.ImplicitParameter,
            Wart.Nothing,
            Wart.Throw,
            Wart.ToString,
            Wart.PlatformDefault
          )
        } else Nil
      },
      Test / compile / wartremoverErrors --= Seq(
        Wart.Any,
        Wart.Equals,
        Wart.GlobalExecutionContext,
        Wart.NonUnitStatements,
        Wart.PublicInference
      ),
      wartremoverExcluded ++= (Compile / routes).value
    )
}
