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
        .returning(w => w)
    }

    def getById(id: String) = quote {
      query[Account].filter(_.id == lift(id))
    }

    val addStringElement = quote { (arr: Seq[String], elem: String) =>
      infix"array_append ( $arr, $elem )".as[Seq[String]]
    }

    def addOrganization(id: String, organization: String) = quote {
      query[Account]
        .filter(_.id == lift(id))
        .update(acc => acc.organizations -> addStringElement(acc.organizations, lift(organization)))
    }

    val removeStringElement = quote { (arr: Seq[String], elem: String) =>
      infix"array_remove ( $arr, $elem )".as[Seq[String]]
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

    def updatePassword(id: String, oldPassword: String, newPassword: String) = ???

    def resetPassword(id: String, newPassword: String) = quote {
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

  override def registerAccount(id: String, password: Option[String], organization: Option[String]): IO[Account] =
    run(Queries.register(id, password, organization)).transact(DoobieTransactor.xa)

  override def getAccountById(id: String): IO[Option[Account]] =
    run(Queries.getById(id)).transact(DoobieTransactor.xa).map(_.headOption)

  override def addOrganizationToAccount(id: String, orgId: String): IO[Long] =
    run(Queries.addOrganization(id, orgId)).transact(DoobieTransactor.xa)

  override def removeOrganizationFromAccount(id: String, orgId: String): IO[Long] =
    run(Queries.removeOrganization(id, orgId)).transact(DoobieTransactor.xa)

  override def removeAllOrganizationFromAccount(id: String): IO[Long] =
    run(Queries.removeAllOrganizations(id)).transact(DoobieTransactor.xa)

  override def updateAccountPassword(id: String, oldPassword: String, newPassword: String): IO[Long] = ???

  override def resetAccountPassword(id: String, password: String): IO[Long] =
    run(Queries.resetPassword(id, password)).transact(DoobieTransactor.xa)

  override def deactivateAccount(id: String): IO[Long] =
    run(Queries.deactivate(id)).transact(DoobieTransactor.xa)

  override def deleteAccount(id: String): IO[Long] =
    run(Queries.delete(id)).transact(DoobieTransactor.xa)
}
