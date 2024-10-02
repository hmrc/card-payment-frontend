resolvers += MavenRepository("HMRC-open-artefacts-maven2", "https://open.artefacts.tax.service.gov.uk/maven2")
resolvers += Resolver.url("HMRC-open-artefacts-ivy2", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always

addSbtPlugin("uk.gov.hmrc"        %  "sbt-auto-build"     % "3.22.0")
addSbtPlugin("uk.gov.hmrc"        %  "sbt-distributables" % "2.5.0")
addSbtPlugin("org.playframework"  %  "sbt-plugin"         % "3.0.3")
addSbtPlugin("org.scoverage"      %  "sbt-scoverage"      % "2.0.12")
addSbtPlugin("org.wartremover"    %  "sbt-wartremover"    % "3.1.5")
addSbtPlugin("org.scalariform"    %% "sbt-scalariform"    % "1.8.3")
addSbtPlugin("com.timushev.sbt"   %  "sbt-updates"        % "0.6.3")
addSbtPlugin("com.typesafe.sbt"   % "sbt-gzip"            % "1.0.2")
addSbtPlugin("io.github.irundaia" % "sbt-sassify"         % "1.5.2")


