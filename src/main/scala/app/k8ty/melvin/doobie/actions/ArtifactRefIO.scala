package app.k8ty.melvin.doobie.actions

import app.k8ty.melvin.doobie.io.ArtifactRef
import cats.effect.IO

trait ArtifactRefIO {
  def insertArtifactRef(ref: ArtifactRef): IO[Long]
  def getArtifactRefs: IO[Seq[ArtifactRef]]
}
