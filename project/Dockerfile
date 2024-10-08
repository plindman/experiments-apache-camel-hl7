# Build step
FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app

# Copy pom from host and download dependencies 
# (this step will be cached as long as pom.xml doesn't change)
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

# Run step
FROM openjdk:11-jre-slim AS run
WORKDIR /app
COPY --from=build /app/target/*.jar ./app.jar
COPY --from=build /app/target/lib ./lib

# Expose the port defined by APP_PORT
EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]