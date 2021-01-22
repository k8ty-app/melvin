package app.k8ty.melvin.models

case class ArtifactListing(
  orgId: String,
  packageId: String,
  version: String
) {
  val href = s"/listings/${orgId.replaceAll("\\.", "/")}/$packageId/$version"
}
