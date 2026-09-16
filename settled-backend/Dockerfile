FROM ghcr.io/graalvm/native-image-community:25i3 AS builder

RUN microdnf install -y maven && microdnf clean all

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean -Pnative native:compile

FROM gcr.io/distroless/base-debian13

WORKDIR /app

COPY --from=builder /app/target/settled-backend /app/settled-backend

EXPOSE 8080

ENTRYPOINT ["/app/settled-backend"]
CMD ["--server.port=8080"]