package app.k8ty.melvin.models

case class Worker(
    organization: Organization,
    friendlyName: Option[String],
    hashedPassword: Option[String]
)
