# syntax=docker/dockerfile:1.7
# Un solo Dockerfile para los 3 microservicios: se elige con --build-arg SERVICE=<modulo>.

# ---------- Etapa 1: compilación y tests (Java 21 + Maven) ----------
FROM maven:3.9-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /workspace

# Primero solo los pom.xml: la descarga de dependencias queda en caché mientras no cambien.
COPY pom.xml .
COPY common/pom.xml common/
COPY user-service/pom.xml user-service/
COPY catalog-service/pom.xml catalog-service/
COPY order-service/pom.xml order-service/
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -q -pl ${SERVICE} -am dependency:go-offline

COPY . .
# Los tests unitarios se ejecutan en el build: si fallan, no se genera la imagen.
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -pl ${SERVICE} -am package \
 && cp ${SERVICE}/target/${SERVICE}-*.jar /workspace/app.jar

# ---------- Etapa 2: runtime mínimo (solo JRE 21) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Nunca ejecutar como root dentro del contenedor.
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /workspace/app.jar app.jar
USER app

EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"
HEALTHCHECK --interval=15s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -qO- http://localhost:8080/actuator/health/readiness || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
