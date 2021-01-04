package app.k8ty.melvin.doobie.io

import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.doobie.actions.OrganizationIO
import cats.effect.IO
import doobie.quill.DoobieContext
import io.getquill.SnakeCase
import doobie.implicits._
import io.getquill.{ idiom => _ }

case class Organization(orgId: String)

object Organization extends OrganizationIO {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  object Queries {
    def create(orgId: String) = quote {
      query[Organization]
        .insert(_.orgId -> lift(orgId))
        .returning(org => org)
    }

    def getById(orgId: String) = quote {
      query[Organization].filter(_.orgId == lift(orgId))
    }

    def delete(orgId: String) = quote {
      query[Organization].filter(_.orgId == lift(orgId)).delete
    }
  }

  override def createOrganization(orgId: String): IO[Organization] =
    run(Queries.create(orgId)).transact(DoobieTransactor.xa)

  override def getOrganizationById(orgId: String): IO[Option[Organization]] =
    run(Queries.getById(orgId)).transact(DoobieTransactor.xa).map(_.headOption)

  override def deleteOrganization(orgId: String): IO[Long] =
    run(Queries.delete(orgId)).transact(DoobieTransactor.xa)
}
