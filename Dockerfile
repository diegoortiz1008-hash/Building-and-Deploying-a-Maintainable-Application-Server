FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src src
RUN mvn -q package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/application-server.jar application-server.jar
EXPOSE 8080
CMD ["java", "-jar", "application-server.jar"]
