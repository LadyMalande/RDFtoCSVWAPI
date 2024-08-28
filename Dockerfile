# Use an official Maven image as the base image
FROM maven:3.9.6-eclipse-temurin-17 AS build
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src

# Copy the local JAR into the container
COPY lib/RDFtoCSV.jar /app/libs/

# Install the local JAR into the Maven repository inside the container
RUN mvn install:install-file \
    -Dfile=/app/libs/RDFtoCSV.jar \
    -DgroupId=com.miklosova.rdftocsvw \
    -DartifactId=RDFtoCSV \
    -Dversion=1.0-SNAPSHOT \
    -Dpackaging=jar

# Build the application using Maven
RUN mvn clean package -DskipTests
# Use an official OpenJDK image as the base image
FROM eclipse-temurin:17
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/RDFtoCSVW-0.0.1-SNAPSHOT-exec.jar .
# Set the command to run the application
CMD ["java", "-jar", "RDFtoCSVW-0.0.1-SNAPSHOT-exec.jar"]
