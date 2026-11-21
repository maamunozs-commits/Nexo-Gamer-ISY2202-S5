FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /proyecto
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package

FROM eclipse-temurin:21-jre
WORKDIR /aplicacion
COPY --from=compilacion /proyecto/target/nexo-gamer.jar .
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "nexo-gamer.jar"]
