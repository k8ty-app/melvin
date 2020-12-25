name := "melvin"
organization := "app.k8ty"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.4"

val http4sVersion = "0.21.13"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "com.github.pureconfig" %% "pureconfig" % "0.14.0"
)

publishTo := Some("Marvin" at "http://localhost:9000/artifacts")