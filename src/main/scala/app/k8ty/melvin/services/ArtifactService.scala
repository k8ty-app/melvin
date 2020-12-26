package app.k8ty.melvin.services

import app.k8ty.melvin.config.StorageConfig
import cats.effect.{Blocker, ContextShift, IO, Timer}
import org.http4s.EntityDecoder.binFile
import org.http4s.dsl.io._
import org.http4s.{HttpRoutes, StaticFile}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import java.io.File

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
  val storageConfig: StorageConfig =
    ConfigSource.default.loadOrThrow[StorageConfig]

  val serviceRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ GET -> rootPath /: artifact => {
      StaticFile
        .fromFile(
          new File(s"${storageConfig.rootStorage}/$artifact"),
          blocker,
          Some(req)
        )
        .getOrElseF(NotFound())
    }
    case req @ PUT -> rootPath /: artifact => {
      val newFile = new File(s"${storageConfig.rootStorage}/$artifact")
      newFile.getParentFile.mkdirs()
      newFile.createNewFile()
      req.decodeWith(binFile(newFile, blocker), strict = true) { _ =>
        Ok("")
      }
    }
  }
}
