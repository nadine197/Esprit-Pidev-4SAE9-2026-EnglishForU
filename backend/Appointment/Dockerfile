# Image de base Java 17
FROM openjdk:17-jdk-alpine
# On définit le répertoire de travail
WORKDIR /app
# On copie le fichier JAR généré par Maven
COPY target/*.jar app.jar
# On expose le port
EXPOSE 8087
# Commande de lancement
ENTRYPOINT ["java", "-jar", "app.jar"]