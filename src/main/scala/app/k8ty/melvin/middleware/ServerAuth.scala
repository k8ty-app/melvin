package app.k8ty.melvin.middleware

import app.k8ty.melvin.doobie.io.Worker
import cats.effect.IO
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.{ BasicCredentials, Request, Response }

object ServerAuth {

  def workerAuthenticated(req: Request[IO])(success: => IO[Response[IO]]): IO[Response[IO]] =
    req.headers.get(Authorization) match {
      case Some(Authorization(BasicCredentials(username, password))) => {
        Worker.verifyBasicCredentials(BasicCredentials(username, password)).flatMap {
          case true  => success
          case false => Forbidden("Verboten")
        }
      }
      case _ => Forbidden("Verboten")
    }

}
