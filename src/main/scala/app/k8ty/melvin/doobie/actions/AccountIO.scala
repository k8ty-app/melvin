package app.k8ty.melvin.doobie.actions

import app.k8ty.melvin.doobie.io.Account
import cats.effect.IO

trait AccountIO {
  def registerAccount(id: String, password: Option[String] = None, organization: Option[String] = None): IO[Account]
  def getAccountById(id: String): IO[Option[Account]]
  def addOrganizationToAccount(id: String, orgId: String): IO[Long]
  def removeOrganizationFromAccount(id: String, orgId: String): IO[Long]
  def removeAllOrganizationFromAccount(id: String): IO[Long]
  def validatePassword(id: String, password: String): IO[Boolean]
  def updateAccountPassword(id: String, oldPassword: String, newPassword: String): IO[Long]
  def resetAccountPassword(id: String, password: String): IO[Long]
  def deactivateAccount(id: String): IO[Long]
  def deleteAccount(id: String): IO[Long]
}
