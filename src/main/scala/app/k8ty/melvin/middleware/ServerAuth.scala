package app.k8ty.melvin.middleware

import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.models.User
import cats.data.{Kleisli, OptionT}
import cats.effect.IO
import doobie.implicits._
import io.getquill.{idiom => _}
import org.http4s.{AuthedRoutes, BasicCredentials, Request}
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator

object ServerAuth {

  val basicRealm: String = "Melvin"
  val dbAuthStore: BasicAuthenticator[IO, User] =
    (credentials: BasicCredentials) => {
      import User.dc._
      run(User.verifyBasicCredentials(credentials))
        .transact(DoobieTransactor.xa)
        .map(_.headOption)
    }
  val basicAuth: AuthMiddleware[IO, String] = BasicAuth(basicRealm, dbAuthStore)
}
