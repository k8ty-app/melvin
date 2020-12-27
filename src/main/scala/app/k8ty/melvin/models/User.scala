package app.k8ty.melvin.models

case class User(
    id: String,
    organizations: List[Organization],
    hashedPassword: Option[String]
)
