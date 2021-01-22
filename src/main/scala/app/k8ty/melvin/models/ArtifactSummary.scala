package app.k8ty.melvin.models

case class ArtifactSummary(
  orgId: String,
  packageId: String,
  version: String
) {
  val href = s"/artifacts/${orgId.replaceAll("\\.", "/")}/$packageId/$version"
}
