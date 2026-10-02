# Образ собирается из готового jar: перед `docker compose build` выполните `./gradlew bootJar`
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY build/libs/redis-game-hub.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
