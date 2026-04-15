# ─── Build Stage ───────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# FIX: Added dependency caching layer before source copy
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Runtime Stage ─────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8056

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8056/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
