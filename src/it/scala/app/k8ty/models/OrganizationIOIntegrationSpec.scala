package app.k8ty.models

import app.k8ty.melvin.doobie.io.Organization
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class OrganizationIOIntegrationSpec extends AnyFlatSpec with should.Matchers {

  val newOrgId: String = "app.k8ty.melvin"
  val newOrg: Organization = Organization(newOrgId)
  val badOrg: String = "d.n.e"
  val miscOrgs: List[String] = List("org.a", "org.b", "org.c")

  "Organization IO" should "be able to create a new organization" in {
    assert(
      Organization.createOrganization(newOrgId).unsafeRunSync() == newOrg
    )
    // Should noe be 4 items in the DB
    miscOrgs.foreach(o => Organization.createOrganization(o).unsafeRunSync())
  }

  it should "return Some[Organization] by orgId if exists" in {
    assert(
      Organization.getOrganizationById(newOrgId).unsafeRunSync().contains(newOrg)
    )
  }

  it should "return None by orgId if it doesn't exist" in {
    assert(
      Organization.getOrganizationById(badOrg).unsafeRunSync().isEmpty
    )
  }

  it should "not affect other rows when deleting a non-existent orgId" in {
    assert(
      Organization.deleteOrganization(badOrg).unsafeRunSync() == 0
    )
  }

  it should "not affect other rows when deleting an existing orgId" in {
    assert(
      Organization.deleteOrganization(newOrgId).unsafeRunSync() == 1
    )
    // Clean up
    miscOrgs.foreach(o => Organization.deleteOrganization(o).unsafeRunSync())
  }


}
