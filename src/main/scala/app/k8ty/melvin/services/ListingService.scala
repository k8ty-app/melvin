package app.k8ty.melvin.services

import app.k8ty.melvin.doobie.io.ArtifactRef
import app.k8ty.melvin.models.ArtifactListing
import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.dsl.io._
import org.http4s.twirl._
import pureconfig.generic.auto._

object ListingService {

  val serviceRoutes: HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "listings" => {
      ArtifactRef.getArtifactRefs.flatMap { results =>
        Ok(
          html.artifacts(
            results
              .map(a => ArtifactListing(a.orgId, a.packageId, a.version))
              .distinct
          )
        )
      }
    }

    case GET -> Root / "listings" / tld / org / pkg / version => {
      val orgId = Seq(tld, org).mkString(".")
      ArtifactRef.getFileList(orgId, pkg, version).flatMap { results =>
        Ok(html.artifact(s""""$orgId" %% "${pkg.split("_")(0)}" % "$version"""", results))
      }
    }

    case GET -> Root / "listings" / tld / org / sub / pkg / version => {
      val orgId = Seq(tld, org, sub).mkString(".")
      ArtifactRef.getFileList(orgId, pkg, version).flatMap { results =>
        Ok(html.artifact(s""""$orgId" %% "${pkg.split("_")(0)}" % "$version"""", results))
      }
    }

  }
}
