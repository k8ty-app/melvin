package app.k8ty.melvin.middleware

import app.k8ty.melvin.doobie.io.Worker
import cats.effect.IO
import org.http4s.dsl.io._
import org.http4s.headers.Authorization
import org.http4s.{ BasicCredentials, Challenge, Request, Response }

object ServerAuth {

  private val forbiddenMessage: String = "You are not authorized to reach this endpoint."

  def workerAuthenticated(req: Request[IO])(success: => IO[Response[IO]]): IO[Response[IO]] =
    req.headers.get(Authorization) match {
      case Some(Authorization(BasicCredentials(username, password))) => {
        Worker.verifyBasicCredentials(username, password).flatMap {
          case true  => success
          case false => Forbidden(forbiddenMessage)
        }
      }
      case _ => Unauthorized(Challenge(scheme = "Basic", realm = "Melvin"))
    }
}
