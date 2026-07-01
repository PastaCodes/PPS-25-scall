scalaVersion := "3.8.4"

lazy val root = rootProject
  .settings(
    name := "scall",
    idePackagePrefix := Some("it.unibo.scall"),
    libraryDependencies ++= Seq(
      "it.unibo.alice.tuprolog" % "2p-core" % "4.1.1",
      "org.scalatest" %% "scalatest" % "3.2.19" % Test,
    )
  )
