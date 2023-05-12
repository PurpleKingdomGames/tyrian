addSbtPlugin("org.xerial.sbt"           %% "sbt-sonatype"             % "3.9.18")
addSbtPlugin("com.github.sbt"           %% "sbt-pgp"                  % "2.2.1")
addSbtPlugin("org.portable-scala"        % "sbt-scalajs-crossproject" % "1.2.0")
addSbtPlugin("org.scala-js"              % "sbt-scalajs"              % "1.13.1")
addSbtPlugin("io.github.davidgregory084" % "sbt-tpolecat"             % "0.4.2")
addSbtPlugin("ch.epfl.scala"             % "sbt-scalafix"             % "0.10.4")
addSbtPlugin("org.scalameta"             % "sbt-mdoc"                 % "2.3.7")
addSbtPlugin("com.github.sbt"            % "sbt-unidoc"               % "0.5.0")
addSbtPlugin("com.github.reibitto"       % "sbt-welcome"              % "0.3.1")
addSbtPlugin("com.github.sbt"            % "sbt-git"                  % "2.0.1")
addSbtPlugin("org.scalameta"             % "sbt-scalafmt"             % "2.5.0")

libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "1.1.1"
