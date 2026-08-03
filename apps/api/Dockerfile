FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src/ src/
RUN ./mvnw package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine AS runtime

RUN addgroup -S codearena && adduser -S codearena -G codearena

WORKDIR /app
COPY --from=build --chown=codearena:codearena \
  /workspace/target/code-arena-api-0.0.1-SNAPSHOT.jar app.jar

USER codearena
EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
