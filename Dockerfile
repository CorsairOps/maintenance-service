FROM eclipse-temurin:21-jdk-alpine-3.22 AS builder
WORKDIR /app
COPY *.jar app.jar

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar", "-Xms128m", "-Xmx256m"]