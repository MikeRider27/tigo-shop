FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine
COPY --from=build /app/target/greeting-1.0.0.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
