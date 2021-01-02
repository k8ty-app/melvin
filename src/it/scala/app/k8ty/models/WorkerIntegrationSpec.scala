package app.k8ty.models

import app.k8ty.melvin.doobie.io.{Organization, Worker}
import io.getquill.{idiom => _}
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class WorkerIntegrationSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfter {

  val orgId: String = "app.worker"
  val badOrg: String = "d.n.e"

  before {
    Organization.createOrganization(orgId).unsafeRunSync()
  }

  after {
    Organization.deleteOrganization(orgId).unsafeRunSync()
  }

  "A Worker" should "not be able to be created if the organization doesn't exist" in {
    assert(
      Worker.createWorker(badOrg, "NoNoNo").unsafeRunSync().isEmpty
    )
  }

  it should "be able to be created if the Organization does exist" in {
    assert(
      Worker.createWorker(orgId, "YesYesYes").unsafeRunSync().nonEmpty
    )
  }
}
