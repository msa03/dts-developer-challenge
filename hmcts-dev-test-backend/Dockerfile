FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY build.gradle gradlew gradlew.bat ./
COPY gradle/ ./gradle/
COPY src/ ./src/
COPY config/ ./config/

RUN apk add --no-cache dos2unix && dos2unix ./gradlew || true \
  && chmod +x ./gradlew \
  && sh ./gradlew clean build -x test -x integration -x checkstyleTest

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 4000

HEALTHCHECK --interval=10s --timeout=5s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:4000/ || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
