package app.k8ty.melvin.doobie

import app.k8ty.melvin.config.DoobieConfig
import cats.effect.{ContextShift, IO}
import doobie.Transactor
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object DoobieTransactor {

  val doobieConfig: DoobieConfig =
    ConfigSource.default.loadOrThrow[DoobieConfig]

  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContexts.synchronous)
  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    doobieConfig.pgDriver,
    doobieConfig.pgUrl,
    doobieConfig.pgUser,
    doobieConfig.pgPass
  )

}
