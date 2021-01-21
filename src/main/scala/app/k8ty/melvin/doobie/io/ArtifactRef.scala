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
)

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
    }

  }

  override def insertArtifactRef(ref: ArtifactRef): IO[Long] =
    run(Queries.insert(ref)).transact(DoobieTransactor.xa)

  override def getArtifactRefs: IO[Seq[ArtifactRef]] =
    run(Queries.get).transact(DoobieTransactor.xa)

}
