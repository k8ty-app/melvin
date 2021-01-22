package app.k8ty.melvin.doobie.io

import app.k8ty.melvin.doobie.DoobieTransactor
import app.k8ty.melvin.doobie.actions.ArtifactRefIO
import cats.effect.IO
import doobie.implicits._
import doobie.quill.DoobieContext
import io.getquill.{ SnakeCase, idiom => _ }

case class ArtifactRef(
  orgId: String,
  packageId: String,
  version: String,
  fileName: String
) {
  val href: String = s"/artifacts/${orgId.replaceAll("\\.","/")}/$packageId/$version/$fileName"
}

object ArtifactRef extends ArtifactRefIO {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._

  object Queries {

    def insert(artifactRef: ArtifactRef) = quote {
      query[ArtifactRef]
        .insert(lift(artifactRef))
    }

    val get = quote {
      query[ArtifactRef]
        .sortBy(a => (a.orgId, a.version, a.packageId))
    }

    def files(orgId: String, packageId: String, version: String) = quote {
      query[ArtifactRef]
        .filter(_.orgId == lift(orgId))
        .filter(_.packageId == lift(packageId))
        .filter(_.version == lift(version))
        .sortBy(_.fileName)
    }

  }

  override def insertArtifactRef(ref: ArtifactRef): IO[Long] =
    run(Queries.insert(ref)).transact(DoobieTransactor.xa)

  override def getArtifactRefs: IO[Seq[ArtifactRef]] =
    run(Queries.get).transact(DoobieTransactor.xa)

  override def getFileList(orgId: String, packageId: String, version: String): IO[Seq[ArtifactRef]] = {
    run(Queries.files(orgId, packageId, version)).transact(DoobieTransactor.xa)
  }

}
