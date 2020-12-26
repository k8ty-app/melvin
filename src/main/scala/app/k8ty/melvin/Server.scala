package app.k8ty.melvin

import app.k8ty.melvin.config.ServerConfig
import app.k8ty.melvin.services.{ApplicationService, ArtifactService}
import cats.data.Kleisli
import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits.toSemigroupKOps
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.{Request, Response}
import pureconfig.ConfigSource
import pureconfig.generic.auto._

import scala.concurrent.ExecutionContext.global

object Server extends IOApp {

  val serverConfig: ServerConfig =
    ConfigSource.default.loadOrThrow[ServerConfig]

  val service: Kleisli[IO, Request[IO], Response[IO]] = {
    ApplicationService.serviceRoutes <+>
      ArtifactService.serviceRoutes
  }.orNotFound

  override def run(args: List[String]): IO[ExitCode] = {
    BlazeServerBuilder[IO](global)
      .bindHttp(serverConfig.port, serverConfig.host)
      .withHttpApp(service)
      .serve
      .compile
      .drain
      .as(ExitCode.Success)

  }

}
