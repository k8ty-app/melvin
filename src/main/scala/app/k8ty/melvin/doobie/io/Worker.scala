package app.k8ty.melvin.doobie.io

import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.doobie.actions.WorkerIO
import cats.data.OptionT
import cats.effect.IO
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import org.http4s.BasicCredentials
import doobie.implicits._
import io.getquill.{ idiom => _ }

import java.util.UUID

case class Worker(
    id: String,
    organization: String,
    name: Option[String],
    secret: Option[String]
)

object Worker extends WorkerIO {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  object Queries {

    def create(orgId: String, name: String) = quote {
      query[Worker]
        .insert(
          _.organization -> lift(orgId),
          _.name -> lift(Option(name)),
          _.secret -> lift(Option(UUID.randomUUID().toString))
        )
        .returning(w => w)
    }
  }

  override def createWorker(orgId: String, name: String): IO[Option[Worker]] = {
    val orgOptIO: IO[Option[Organization]] = Organization.getOrganizationById(orgId)
    val createIO: IO[Worker] = run(Queries.create(orgId, name)).transact(DoobieTransactor.xa)
    orgOptIO.flatMap {
      case Some(_) => {
        OptionT.liftF(createIO).value
      }
      case None => IO(None)
    }
  }

  override def verifyBasicCredentials(credentials: BasicCredentials): IO[Boolean] = ???

  override def renameWorker(id: String, name: String): IO[Long] = ???

  override def reRollWorkerSecret(id: String): IO[Option[String]] = ???

  override def deleteWorker(id: String): IO[Long] = ???
}
