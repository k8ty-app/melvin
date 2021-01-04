package app.k8ty.melvin.storage

import app.k8ty.melvin.config.S3StorageConfig
import cats.effect.{ IO, Resource }
import io.minio.http.Method
import io.minio.{ GetPresignedObjectUrlArgs, MinioClient, PutObjectArgs }
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import java.io.ByteArrayInputStream
import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.jdk.CollectionConverters._
object S3StorageProvider {

  val storageConfig: S3StorageConfig =
    ConfigSource.default.loadOrThrow[S3StorageConfig]

  def resource: Resource[IO, MinioClient] = Resource.liftF {
    IO {
      MinioClient
        .builder()
        .endpoint(storageConfig.endpoint)
        .credentials(
          storageConfig.accessKey,
          storageConfig.secretKey
        )
        .build()
    }
  }

  def storeObject(path: String, data: Array[Byte])(implicit client: MinioClient): IO[Either[String, String]] = IO {
    Try {
      client
        .putObject(
          PutObjectArgs
            .builder()
            .bucket(storageConfig.bucket)
            .`object`(path)
            .headers(Map("x-amz-acl" -> "public-read").asJava)
            .stream(new ByteArrayInputStream(data), data.length, -1)
            .build()
        )
        .etag()
    }.toEither.left.map(_.getMessage)
  }

  def getSignedUrl(path: String)(implicit client: MinioClient): IO[Either[String, String]] = IO {
    Try {
      client.getPresignedObjectUrl(
        GetPresignedObjectUrlArgs
          .builder()
          .method(Method.GET)
          .bucket(storageConfig.bucket)
          .`object`(path)
          .expiry(1, TimeUnit.MINUTES)
          .build()
      )
    }.toEither.left.map(_.getMessage)
  }

}
