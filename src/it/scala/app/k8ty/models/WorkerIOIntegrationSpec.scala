package app.k8ty.models

import app.k8ty.melvin.doobie.io.{ Organization, Worker }
import io.getquill.{ idiom => _ }
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class WorkerIOIntegrationSpec extends AnyFlatSpec with should.Matchers with BeforeAndAfter {

  val orgId: String        = "app.worker"
  val badOrg: String       = "d.n.e"
  val originalName: String = "ogName"

  // Not in a before, due to our lazy vals
  before {
    Organization.createOrganization(orgId).unsafeRunSync()
  }

  after {
    Organization.deleteOrganization(orgId).unsafeRunSync()
  }

  "WorkerIO" should "not be able to create if the Organization doesn't exist" in {
    assert(
      Worker.createWorker(badOrg, "NoNoNo").unsafeRunSync().isEmpty
    )
  }

  lazy val originalWorker: Option[Worker] = Worker.createWorker(orgId, originalName).unsafeRunSync()

  it should "be able to create and update a Worker" in {
    // Make sure we create properly
    assert(
      originalWorker.nonEmpty
    )
    assert(
      originalWorker.map(_.organization).contains(orgId)
    )
    assert(
      originalWorker.map(_.name).contains(originalName)
    )
  }

  lazy val workerId: String       = originalWorker.map(_.id).getOrElse(throw new Exception("No Worker ID!"))
  lazy val originalSecret: String = originalWorker.flatMap(_.secret).getOrElse(throw new Exception("No Worker Secret"))

  it should "be able to get a Worker by ID" in {
    assert(
      Worker.getWorkerById(workerId).unsafeRunSync().nonEmpty
    )
  }

  it should "properly verify Correct BasicCredentials" in {
    assert(
      Worker.verifyBasicCredentials(workerId, originalSecret).unsafeRunSync().isDefined
    )
  }

  it should "properly verify Bad User BasicCredentials" in {
    assert(
      Worker.verifyBasicCredentials("No Good", originalSecret).unsafeRunSync().isEmpty
    )
  }

  it should "properly verify Bad Password BasicCredentials" in {
    assert(
      Worker.verifyBasicCredentials(workerId, "No Good").unsafeRunSync().isEmpty
    )
  }

  it should "properly verify Bad User + Pass BasicCredentials" in {
    assert(
      Worker.verifyBasicCredentials("No good", "No Good").unsafeRunSync().isEmpty
    )
  }

  lazy val newWorkerSecret: Option[String] = Worker.reRollWorkerSecret(workerId).unsafeRunSync()
  lazy val newSecret: String                = newWorkerSecret.getOrElse(throw new Exception("No updated Worker Secret"))

  it should "be able to re-roll a Worker secret" in {
    assert(newWorkerSecret.nonEmpty)
    assert(!newWorkerSecret.contains(originalSecret))
  }

  it should "properly verify our updated BasicCredentials" in {
    assert(
      Worker.verifyBasicCredentials(workerId, originalSecret).unsafeRunSync().isEmpty
    )
    assert(
      Worker.verifyBasicCredentials(workerId, newSecret).unsafeRunSync().isDefined
    )
  }

  it should "be able to rename a Worker" in {
    assert(
      Worker.renameWorker(workerId, "new name").unsafeRunSync() == 1
    )
  }

  it should "be able to deactivate a worker" in {
    assert(
      Worker.deactivateWorker(workerId).unsafeRunSync() == 1
    )
    assert(
      Worker.verifyBasicCredentials(workerId, newSecret).unsafeRunSync().isEmpty
    )
  }

  it should "be able to delete a Worker" in {
    assert(
      Worker.deleteWorker(workerId).unsafeRunSync() == 1
    )
  }

}
