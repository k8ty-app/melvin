package app.k8ty.melvin.services

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.twirl._
object ApplicationService {

  val serviceRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => {
      Ok(html.index())
    }

    case GET -> Root / "login" => {
      Ok("Not Implemented yet!")
    }

    case req @ POST -> Root / "login" => {
      Ok("Not Implemented yet!")
    }

    case GET -> Root / "logout" => {
      Ok("Not Implemented yet!")
    }

    case GET -> Root / "register" => {
      Ok("Not Implemented yet!")
    }

    case POST -> Root / "register" => {
      Ok("Not Implemented yet!")
    }

    case GET -> Root / "workers" => {
      Ok("Not Implemented yet!")
    }

    case req @ POST -> "workers" /: org => {
      Ok("Not Implemented yet!")
    }

  }

}
