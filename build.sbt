scalaVersion := "3.8.4"

lazy val root = rootProject
  .dependsOn(scall, finf)
  .settings(
    name := "scall-demo",
    ThisBuild / version := "1.0.0",
    ThisBuild / organization := "it.unibo",
    Compile / mainClass := Some("it.unibo.finf.run"),
  )

lazy val scall = project
  .in(file("scall"))
  .settings(
    name := "scall",
    idePackagePrefix := Some("it.unibo.scall"),
    libraryDependencies ++= Seq(
      "it.unibo.alice.tuprolog" % "2p-core" % "4.1.1",
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "com.lihaoyi" %% "sourcecode" % "0.4.4",
    ),
  )

lazy val finf = project
  .in(file("finf"))
  .settings(
    name := "finf",
    idePackagePrefix := Some("it.unibo.finf"),
    Compile / scalaSource := baseDirectory.value,
  )
  .dependsOn(scall)
