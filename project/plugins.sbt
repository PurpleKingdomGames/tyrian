addSbtPlugin("org.xerial.sbt"     %% "sbt-sonatype"             % "3.10.0")
addSbtPlugin("com.github.sbt"     %% "sbt-pgp"                  % "2.2.1")
addSbtPlugin("org.portable-scala"  % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("org.scala-js"        % "sbt-scalajs"              % "1.17.0")
addSbtPlugin("org.typelevel"       % "sbt-tpolecat"             % "0.5.2")
addSbtPlugin("ch.epfl.scala"       % "sbt-scalafix"             % "0.12.1")
addSbtPlugin("com.github.reibitto" % "sbt-welcome"              % "0.4.0")
addSbtPlugin("com.github.sbt"      % "sbt-git"                  % "2.0.1")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"             % "2.5.2")
addSbtPlugin("com.timushev.sbt"    % "sbt-updates"              % "0.6.4")

libraryDependencies += "org.scala-js" %% "scalajs-env-selenium" % "1.1.1"
