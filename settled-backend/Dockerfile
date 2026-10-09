FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /build

COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B dependency:go-offline

COPY src ./src

RUN --mount=type=cache,target=/root/.m2/repository \
    mvn -B clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY --from=builder /build/target/settled-backend-0.0.1-SNAPSHOT.jar app.jar
COPY --from=builder /build/src/main/resources/db/migration ./db/migration

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]   