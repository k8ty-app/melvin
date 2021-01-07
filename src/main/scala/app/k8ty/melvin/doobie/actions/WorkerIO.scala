package app.k8ty.melvin.doobie.actions

import app.k8ty.melvin.doobie.io.Worker
import cats.effect.IO

trait WorkerIO {
  def createWorker(orgId: String, name: String): IO[Option[Worker]]
  def verifyBasicCredentials(id: String, secret: String): IO[Boolean]
  def renameWorker(id: String, name: String): IO[Long]
  def reRollWorkerSecret(id: String): IO[Option[String]]
  def deleteWorker(id: String): IO[Long]
  def deactivateWorker(id: String): IO[Long]
  def getWorkerById(id: String): IO[Option[Worker]]
}
