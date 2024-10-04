#!/bin/bash

# Create the main project directory
mkdir -p project

# Change to the project directory
cd project

# Create the directory structure
mkdir -p src/main/java/com/example
mkdir -p src/main/resources

# Create empty Java files
touch src/main/java/com/example/ObservationSimulator.java
touch src/main/java/com/example/HL7V2Converter.java
touch src/main/java/com/example/HL7FHIRConverter.java

# Create empty resources file
touch src/main/resources/application.properties

# Create empty pom.xml
touch pom.xml

# Create empty Dockerfile
touch Dockerfile

echo "Project structure initialized successfully."