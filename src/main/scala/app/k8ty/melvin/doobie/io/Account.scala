package app.k8ty.melvin.doobie.io

import app.k8ty.crypto.Crypto
import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.doobie.actions.AccountIO
import cats.effect.IO
import doobie.quill.DoobieContext
import io.getquill.{ SnakeCase, idiom => _ }
import doobie.implicits._

case class Account(
  id: String,
  organizations: Seq[String],
  hashedPassword: Option[String]
)

object Account extends AccountIO {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  object Queries {

    def register(id: String, password: Option[String], organization: Option[String]) = quote {
      query[Account]
        .insert(
          _.id             -> lift(id),
          _.hashedPassword -> lift(password.map(p => Crypto.pbkdf2Hash(p))),
          _.organizations  -> lift(organization.toList)
        )
        .returning(a => a)
    }

    def getById(id: String) = quote {
      query[Account].filter(_.id == lift(id))
    }

    val addStringElement = quote { (arr: Seq[String], elem: String) =>
      infix"array_append ( $arr, $elem::text )".as[Seq[String]]
    }

    def addOrganization(id: String, organization: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .update(acc => acc.organizations -> addStringElement(acc.organizations, lift(organization)))
    }

    val removeStringElement = quote { (arr: Seq[String], elem: String) =>
      infix"array_remove ( $arr, $elem::text )".as[Seq[String]]
    }

    def removeOrganization(id: String, organization: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .update(acc => acc.organizations -> removeStringElement(acc.organizations, lift(organization)))
    }

    def removeAllOrganizations(id: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .update(_.organizations -> lift(List.empty[String]))
    }

    def getHashedPassword(id: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .take(1)
        .map(_.hashedPassword)
    }

    def updatePassword(id: String, newPassword: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .update(_.hashedPassword -> lift(Option(Crypto.pbkdf2Hash(newPassword))))
    }

    def deactivate(id: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .update(_.hashedPassword -> None)
    }

    def delete(id: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .delete
    }

  }

  override def registerAccount(id: String, password: Option[String], organization: Option[String]): IO[Option[Account]] = {
    organization match {
      case Some(orgId) => {
        Organization.getOrganizationById(orgId).flatMap { org =>
          run(Queries.register(id, password, org.map(_.orgId))).transact(DoobieTransactor.xa).option
        }
      }
      case None => run(Queries.register(id, password, organization)).transact(DoobieTransactor.xa).option
    }
  }

  override def getAccountById(id: String): IO[Option[Account]] =
    run(Queries.getById(id)).transact(DoobieTransactor.xa).map(_.headOption)

  override def addOrganizationToAccount(id: String, orgId: String): IO[Long] =
    Organization.getOrganizationById(orgId).flatMap {
      case Some(org) => run(Queries.addOrganization(id, org.orgId)).transact(DoobieTransactor.xa)
      case None => IO.pure(0L)
    }

  override def removeOrganizationFromAccount(id: String, orgId: String): IO[Long] =
    run(Queries.removeOrganization(id, orgId)).transact(DoobieTransactor.xa)

  override def removeAllOrganizationFromAccount(id: String): IO[Long] =
    run(Queries.removeAllOrganizations(id)).transact(DoobieTransactor.xa)

  override def updateAccountPassword(id: String, oldPassword: String, newPassword: String): IO[Long] = {
    validatePassword(id, oldPassword).flatMap {
      case true  => run(Queries.updatePassword(id, newPassword)).transact(DoobieTransactor.xa)
      case false => IO.pure(0L)
    }
  }

  override def resetAccountPassword(id: String, password: String): IO[Long] =
    run(Queries.updatePassword(id, password)).transact(DoobieTransactor.xa)

  override def deactivateAccount(id: String): IO[Long] =
    run(Queries.deactivate(id)).transact(DoobieTransactor.xa)

  override def deleteAccount(id: String): IO[Long] =
    run(Queries.delete(id)).transact(DoobieTransactor.xa)

  override def validatePassword(id: String, password: String): IO[Boolean] =
    run(Queries.getHashedPassword(id))
      .transact(DoobieTransactor.xa)
      .map(_.headOption.flatten.exists(h => Crypto.validatePbkdf2Hash(password, h)))
}
