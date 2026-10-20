# skillupcloud
Learning AWS with many corners

## Spring Boot ping application

This Maven project exposes a simple health-check endpoint:

```text
GET http://localhost:8080/ping
```

The endpoint responds with `pong`. Run the application with:

```sh
mvn spring-boot:run
```

The project requires Java 21 or later.

Run the tests with:

```sh
mvn test
```

## S3 JSON file API

Configure the S3 bucket and region with `S3_BUCKET` and `AWS_REGION`. The AWS SDK
uses its default credential provider chain; use an IAM role or other short-lived
credentials rather than storing AWS keys in the application.

Upload a JSON file as multipart form data using the `file` field:

```sh
curl -F "file=@payload.json;type=application/json" http://localhost:8080/addFile
```

The uploaded file's name is used as its S3 object key. Read it back with:

```sh
curl "http://localhost:8080/readFile?fileName=payload.json"
```

Uploading another file with the same name replaces the existing object.

The application's IAM identity needs `s3:PutObject` and `s3:GetObject` access
limited to the configured bucket and required object prefix. S3 requests use
HTTPS; retain S3's default encryption or configure the bucket to use SSE-KMS as
required. For operations visibility, enable S3 server access logging, CloudTrail
data events, and CloudWatch request metrics. S3 storage, request, and monitoring
charges may apply; see
[Amazon S3 pricing](https://aws.amazon.com/s3/pricing/).
