package app.k8ty.melvin.doobie.io

import app.k8ty.crypto.Crypto
import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.doobie.actions.WorkerIO
import cats.data.OptionT
import cats.effect.IO
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import doobie.implicits._
import io.getquill.{ idiom => _ }

case class Worker(
  id: String,
  organization: String,
  name: String,
  secret: Option[String]
)

object Worker extends WorkerIO {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  object Queries {

    def create(orgId: String, name: String) = quote {
      query[Worker]
        .insert(
          _.id           -> lift(Crypto.generatePublicKey),
          _.organization -> lift(orgId),
          _.name         -> lift(name),
          _.secret       -> lift(Option(Crypto.generatePrivateKey))
        )
        .returning(w => w)
    }

    def get(id: String) = quote {
      query[Worker]
        .filter(_.id == lift(id))
    }

    def verify(id: String, secret: String) = quote {
      query[Worker]
        .filter(_.id == lift(id))
        .filter(_.secret.contains(lift(secret)))
        .nonEmpty
    }

    def rename(id: String, name: String) = quote {
      query[Worker]
        .filter(_.id == lift(id))
        .update(_.name -> lift(name))
    }

    def rollSecret(id: String) = quote {
      query[Worker]
        .filter(_.id == lift(id))
        .update(_.secret -> lift(Option(Crypto.generatePrivateKey)))
        .returning(w => w.secret)
    }

    def delete(id: String) = quote {
      query[Worker]
        .filter(_.id == lift(id))
        .delete
    }

    def deactivate(id: String) = quote {
      query[Worker]
        .filter(_.id == lift(id))
        .update(_.secret -> None)
    }

  }

  override def createWorker(orgId: String, name: String): IO[Option[Worker]] = {
    val orgOptIO: IO[Option[Organization]] = Organization.getOrganizationById(orgId)
    val createIO: IO[Worker]               = run(Queries.create(orgId, name)).transact(DoobieTransactor.xa)
    orgOptIO.flatMap {
      case Some(_) => {
        OptionT.liftF(createIO).value
      }
      case None    => IO(None)
    }
  }

  override def verifyBasicCredentials(id: String, secret: String): IO[Boolean] =
    run(Queries.verify(id, secret)).transact(DoobieTransactor.xa)

  override def renameWorker(id: String, name: String): IO[Long] =
    run(Queries.rename(id, name)).transact(DoobieTransactor.xa)

  override def reRollWorkerSecret(id: String): IO[Option[String]] =
    run(Queries.rollSecret(id)).transact(DoobieTransactor.xa)

  override def deleteWorker(id: String): IO[Long] =
    run(Queries.delete(id)).transact(DoobieTransactor.xa)

  override def deactivateWorker(id: String): IO[Long] =
    run(Queries.deactivate(id)).transact(DoobieTransactor.xa)

  override def getWorkerById(id: String): IO[Option[Worker]] =
    run(Queries.get(id)).transact(DoobieTransactor.xa).map(_.headOption)
}
