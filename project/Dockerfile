FROM maven:3.8.4-openjdk-11-slim AS build
WORKDIR /app
COPY pom.xml .

# Download dependencies (this step will be cached as long as pom.xml doesn't change)
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package

FROM openjdk:11-jre-slim AS run
WORKDIR /app
COPY --from=build /app/target/camel-hl7-simulator-1.0-SNAPSHOT.jar ./app.jar
COPY --from=build /app/target/lib ./lib

# Set default values for environment variables
ENV APP_PORT=8080
ENV SCHEDULER_ENABLED=false

# Expose the port defined by APP_PORT
EXPOSE ${APP_PORT}

CMD ["sh", "-c", "java -jar app.jar --app.port=${APP_PORT} --scheduler.enabled=${SCHEDULER_ENABLED}"]