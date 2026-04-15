# Remplace la ligne 1 par :
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app
COPY target/*.jar app.jar
# (Garde le reste : EXPOSE et ENTRYPOINT selon le service)
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]