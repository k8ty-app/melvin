package app.k8ty.melvin.services

import cats.data.NonEmptyList
import cats.effect.{ Blocker, ContextShift, IO, Timer }
import org.http4s.CacheDirective.`max-age`
import org.http4s.dsl.io._
import org.http4s.headers.`Cache-Control`
import org.http4s.{ HttpRoutes, StaticFile }

import scala.concurrent.duration.{ DAYS, Duration }

object AssetService {

  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(scala.concurrent.ExecutionContext.Implicits.global)

  implicit val ioTimer: Timer[IO] =
    IO.timer(scala.concurrent.ExecutionContext.Implicits.global)

  val blocker: Blocker =
    Blocker.liftExecutionContext(
      scala.concurrent.ExecutionContext.Implicits.global
    )

  val serviceRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ _ -> "assets" /: _ => {
      StaticFile
        .fromResource(req.pathInfo, blocker, Some(req))
        .map(_.putHeaders())
        .map(_.putHeaders(`Cache-Control`(NonEmptyList.of(`max-age`(Duration(7, DAYS))))))
        .getOrElseF(NotFound(req.pathInfo))
    }
  }

}
