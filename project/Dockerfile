# Step: Load dependencies step
FROM maven:3.8.6-openjdk-11-slim AS dependencies
WORKDIR /app
# Copy pom from host and download dependencies 
# (this step will be cached as long as pom.xml doesn't change)
COPY pom.xml .
RUN mvn dependency:resolve
RUN mvn dependency:go-offline -B

# Step: Build
FROM maven:3.8.6-openjdk-11-slim AS build
WORKDIR /app

COPY --from=dependencies /root/.m2 /root/.m2

COPY pom.xml .
COPY src ./src

# Build application using cached dependencies
# -o = offline. Force failure if problem with dependencies
RUN mvn clean package -DskipTests -o

# Step: Run
FROM openjdk:11-jre-slim AS run
WORKDIR /appexit
COPY --from=build /app/target/*.jar ./app.jar
COPY --from=build /app/target/lib ./lib

# Expose the port defined by APP_PORT
EXPOSE ${APP_PORT}

ENTRYPOINT ["java", "-jar", "app.jar"]