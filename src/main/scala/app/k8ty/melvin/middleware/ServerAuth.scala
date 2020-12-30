package app.k8ty.melvin.middleware

import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.models.Account
import cats.data.{ Kleisli, OptionT }
import cats.effect.IO
import doobie.implicits._
import io.getquill.{ idiom => _ }
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.server.middleware.authentication.BasicAuth
import org.http4s.server.middleware.authentication.BasicAuth.BasicAuthenticator
import org.http4s.{ AuthedRoutes, BasicCredentials, Request }

object ServerAuth {

  val basicRealm: String = "Melvin"
  val dbAuthStore: BasicAuthenticator[IO, Account] =
    (credentials: BasicCredentials) => {
      import Account.dc._
      run(Account.verifyBasicCredentials(credentials))
        .transact(DoobieTransactor.xa)
        .map(_.headOption)
    }

  val authUser: Kleisli[IO, Request[IO], Either[String, Account]] =
    BasicAuth
      .challenge(basicRealm, dbAuthStore)
      .map(_.left.map(_ => "verboten").map(_.context))
  val onFailure: AuthedRoutes[String, IO] = Kleisli(req => OptionT.liftF(Forbidden(req.context)))
  val basicAuth: AuthMiddleware[IO, Account] =
    AuthMiddleware(authUser, onFailure)
}
