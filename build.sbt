name := "melvin"
organization := "app.k8ty"
version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.4"

val http4sVersion = "0.21.13"
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-twirl" % http4sVersion,
  "com.github.pureconfig" %% "pureconfig" % "0.14.0",
  "org.postgresql" % "postgresql" % "42.2.18"
)

lazy val root = (project in file(".")).enablePlugins(SbtTwirl)

enablePlugins(FlywayPlugin)
flywayUrl := sys.env.getOrElse("PG_URL",
                               "jdbc:postgresql://localhost:5432/postgres")
flywayUser := sys.env.getOrElse("PG_USER", "postgres")
flywayPassword := sys.env.getOrElse("PG_PASS", "password")
flywayLocations += "db/migration"
