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
  }

}
