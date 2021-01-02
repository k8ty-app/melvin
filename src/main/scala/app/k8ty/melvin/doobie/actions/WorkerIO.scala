package app.k8ty.melvin.doobie.actions

import app.k8ty.melvin.doobie.io.Worker
import cats.effect.IO
import org.http4s.BasicCredentials

trait WorkerIO {
  def createWorker(orgId: String, name: String): IO[Option[Worker]]
  def verifyBasicCredentials(credentials: BasicCredentials): IO[Boolean]
  def renameWorker(id: String, name: String): IO[Long]
  def reRollWorkerSecret(id: String): IO[Option[String]]
  def deleteWorker(id: String): IO[Long]
}
