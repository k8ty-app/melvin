package app.k8ty.melvin.services

import app.k8ty.melvin.doobie.io.ArtifactRef
import app.k8ty.melvin.middleware.ServerAuth.workerAuthenticated
import app.k8ty.melvin.storage.S3StorageProvider
import cats.effect.{Blocker, ContextShift, IO}
import org.http4s.EntityDecoder.byteArrayDecoder
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, Request, Response, StaticFile}
import pureconfig.generic.auto._

import java.net.URL

object ArtifactService {

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  val blocker: Blocker =
    Blocker.liftExecutionContext(
      scala.concurrent.ExecutionContext.Implicits.global
    )

  protected def s3StoreResult(req: Request[IO], s3Path: String): IO[Response[IO]] = {
    req.decodeWith(byteArrayDecoder, strict = true) { data =>
      S3StorageProvider.resource
        .use { implicit client =>
          S3StorageProvider.storeObject(s3Path, data)
        }
        .flatMap {
          case Right(etag) => Ok(etag)
          case Left(err)   => InternalServerError(err)
        }
    }
  }

  val serviceRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case req @ GET -> "artifacts" /: artifact                                          => {
      S3StorageProvider.resource
        .use { implicit client =>
          S3StorageProvider.getSignedUrl(s"artifacts/$artifact")
        }
        .flatMap {
          case Right(url) => {
            StaticFile
              .fromURL(new URL(url), blocker, Some(req))
              .getOrElseF(NotFound())
          }
          case Left(err)  => InternalServerError(err)
        }
    }
    case req @ PUT -> Root / "artifacts" / tld / org / artifact / version / file ~ ext => {
      workerAuthenticated(req) { worker =>
        if (worker.organization.equals(Seq(tld, org).mkString("."))) {
          val s3Path = s"artifacts/$tld/$org/$artifact/$version/$file.$ext"
          val ref    = ArtifactRef(
            Seq(tld, org).mkString("."),
            artifact,
            version,
            Seq(file, ext).mkString(".")
          )
          for {
            _      <- ArtifactRef.insertArtifactRef(ref).handleErrorWith(_ => IO.unit)
            result <- s3StoreResult(req, s3Path)
          } yield result
        } else {
          Forbidden("Worker is not a member of this organization")
        }
      }
    }

    case req @ PUT -> Root / "artifacts" / tld / org / sub / artifact / version / file ~ ext => {
      workerAuthenticated(req) { worker =>
        if (worker.organization.equals(Seq(tld, org, sub).mkString("."))) {
          val s3Path: String = s"artifacts/$tld/$org/$sub/$artifact/$version/$file.$ext"
          val ref            = ArtifactRef(
            Seq(tld, org, sub).mkString("."),
            artifact,
            version,
            Seq(file, ext).mkString(".")
          )
          for {
            _      <- ArtifactRef.insertArtifactRef(ref).handleErrorWith(_ => IO.unit)
            result <- s3StoreResult(req, s3Path)
          } yield result
        } else {
          Forbidden("Worker is not a member of this organization")
        }
      }
    }
  }
}
