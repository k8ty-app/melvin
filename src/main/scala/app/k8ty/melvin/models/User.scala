package app.k8ty.melvin.models

import doobie.quill.DoobieContext
import org.http4s.BasicCredentials
import io.getquill.{SnakeCase, idiom => _}

case class User(
    id: String,
    organizations: List[Organization],
    hashedPassword: Option[String]
)

object User {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._
  val verifyBasicCredentials: BasicCredentials => Quoted[EntityQuery[User]] =
    credentials =>
      quote {
        query[User]
          .filter(_.id == credentials.username)
          .filter(
            _.hashedPassword.contains(credentials.password)
          ) // TODO hash, not plain
      }
}
