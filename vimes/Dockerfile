# Stage 1: Build Application JAR
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle .
COPY src src

RUN chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# Stage 2: Production Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

ENV PORT=3000
EXPOSE 3000

COPY --from=builder /app/build/libs/*.jar app.jar

HEALTHCHECK --interval=10s --timeout=5s --start-period=15s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:3000/api/v1/healthz || exit 1

ENTRYPOINT ["java", "-Dfile.encoding=UTF-8", "-Xms256m", "-Xmx512m", "-jar", "app.jar"]
