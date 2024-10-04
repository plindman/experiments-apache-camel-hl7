FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM openjdk:11-jre-slim
WORKDIR /app
COPY --from=build /app/target/camel-hl7-simulator-1.0-SNAPSHOT.jar ./app.jar
COPY --from=build /app/target/lib ./lib
CMD ["java", "-jar", "app.jar"]