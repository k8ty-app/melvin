package app.k8ty.melvin.models

import doobie.quill.DoobieContext
import io.getquill.{SnakeCase, idiom => _}
import org.http4s.BasicCredentials

case class Account(
    id: String,
//    organizations: List[Organization],
    hashedPassword: Option[String]
)

object Account {
  val dc = new DoobieContext.Postgres(SnakeCase)
  import dc._
  val verifyBasicCredentials: BasicCredentials => Quoted[EntityQuery[Account]] = {
    credentials => {
      quote {
        query[Account]
          .filter(_.id == lift(credentials.username))
          .filter(
            _.hashedPassword.contains(lift(credentials.password))
          ) // TODO hash, not plain
      }
    }
  }
}
