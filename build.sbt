lazy val http4sVersion = "0.21.13"
lazy val doobieVersion = "0.9.0"
lazy val scalatest = "org.scalatest" %% "scalatest" % "3.2.2"
lazy val root = (project in file("."))
  .enablePlugins(DockerPlugin, JavaServerAppPackaging, SbtTwirl)
  .configs(IntegrationTest)
  .settings(
    name := "melvin",
    organization := "app.k8ty",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.4",
    Defaults.itSettings,
    libraryDependencies += scalatest % "it,test",
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-twirl" % http4sVersion,
      "com.github.pureconfig" %% "pureconfig" % "0.14.0",
      "org.postgresql" % "postgresql" % "42.2.18",
      "org.tpolecat" %% "doobie-core" % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-specs2" % doobieVersion,
      "org.tpolecat" %% "doobie-quill" % doobieVersion,
      "io.minio" % "minio" % "8.0.3"
    )
  )

enablePlugins(FlywayPlugin)
flywayUrl := sys.env.getOrElse("PG_URL", "jdbc:postgresql://localhost:5432/postgres")
flywayUser := sys.env.getOrElse("PG_USER", "postgres")
flywayPassword := sys.env.getOrElse("PG_PASS", "password")
flywayLocations += "db/migration"

dockerExposedPorts ++= Seq(9000)
dockerBaseImage := "adoptopenjdk/openjdk15"
