package app.k8ty.melvin.doobie.actions

import app.k8ty.melvin.doobie.io.Organization
import cats.effect.IO

trait OrganizationIO {
  def createOrganization(orgId: String): IO[Organization]
  def getOrganizationById(orgId: String): IO[Option[Organization]]
  def deleteOrganization(orgId: String): IO[Long]
}
