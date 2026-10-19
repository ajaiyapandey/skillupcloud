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

The project requires Java 25 LTS.

Run the tests with:

```sh
mvn test
```
