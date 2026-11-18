FROM maven:3.9-eclipse-temurin-17 AS build
LABEL authors="niyazshakeel"
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

RUN useradd --system --no-create-home appuser
COPY --from=build /app/target/store-1.0.0.jar app.jar
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]