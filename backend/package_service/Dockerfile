# ─── Build Stage ───────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# FIX: Added dependency caching layer (was missing, causing slow rebuilds)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Runtime Stage ─────────────────────────────────────────────
# FIX: Replaced eclipse-temurin:17-jdk-jammy with JRE-only image (smaller & more secure)
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
COPY --from=build /app/target/*.jar app.jar
RUN chown appuser:appgroup app.jar
USER appuser

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]