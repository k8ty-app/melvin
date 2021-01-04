package app.k8ty.melvin.config

case class S3StorageConfig(
    endpoint: String,
    accessKey: String,
    secretKey: String,
    bucket: String
)
