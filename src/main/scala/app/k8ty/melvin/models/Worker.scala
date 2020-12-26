package app.k8ty.melvin.models

case class Worker(
    organization: Organization,
    friendlyName: String,
    password: String,
    expires: Long
)
