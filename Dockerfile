FROM golang:1.27-alpine AS builder

WORKDIR /app

RUN apk add --no-cache curl && \
    curl -L https://github.com/golang-migrate/migrate/releases/download/v4.19.1/migrate.linux-amd64.tar.gz | tar xvz -C /usr/local/bin && \
    rm -rf /var/cache/apk/*

COPY go.mod go.sum ./

RUN go mod download

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app/bin/settled ./cmd/main

FROM alpine:3.24

WORKDIR /app

RUN apk add --no-cache ca-certificates && \
    rm -rf /var/cache/apk/*

COPY --from=builder /app/bin/settled /app/settled

COPY migrations/ /app/migrations/

HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/health || exit 1

EXPOSE 8080

ENTRYPOINT ["/app/settled"]