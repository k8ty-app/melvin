package app.k8ty.melvin.services

import app.k8ty.melvin.storage.S3StorageProvider
import cats.effect.{ Blocker, ContextShift, IO, Timer }
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.dsl.io._
import org.http4s.{ HttpRoutes, StaticFile }
import pureconfig.generic.auto._

import java.net.URL

object ArtifactService {

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)
  implicit val ioTimer: Timer[IO] =
    IO.timer(scala.concurrent.ExecutionContext.Implicits.global)
  val blocker: Blocker =
    Blocker.liftExecutionContext(
      scala.concurrent.ExecutionContext.Implicits.global
    )

  val rootPath: String = "artifacts"

  val serviceRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> rootPath /: artifact => {
      S3StorageProvider.resource
        .use { implicit client =>
          S3StorageProvider.getSignedUrl(s"$rootPath$artifact")
        }
        .flatMap {
          case Right(url) => {
            StaticFile
              .fromURL(new URL(url), blocker, Some(req))
              .getOrElseF(NotFound())
          }
          case Left(err) => InternalServerError(err)
        }
    }
    case req @ PUT -> rootPath /: artifact => {
      req.decodeWith(byteArrayDecoder, strict = true) { data =>
        S3StorageProvider.resource
          .use { implicit client =>
            S3StorageProvider.storeObject(s"$rootPath$artifact", data)
          }
          .flatMap {
            case Right(etag) => Ok(etag)
            case Left(err)   => InternalServerError(err)
          }
      }
    }
  }
}
