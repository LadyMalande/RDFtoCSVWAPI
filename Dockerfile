# Use an official Maven image as the base image
FROM maven:3.9.9-eclipse-temurin-21 AS build
# Set the working directory in the container
WORKDIR /app
# Copy the pom.xml and the project files to the container
COPY pom.xml .
COPY src ./src
COPY application.properties ./
COPY lib/extracted-pom.xml /app/libs/

# Copy the local JAR into the container
COPY lib/RDFtoCSV-1.0-SNAPSHOT.jar /app/libs/

# Install the local JAR into the Maven repository inside the container
RUN mvn install:install-file \
    -Dfile=/app/libs/RDFtoCSV-1.0-SNAPSHOT.jar \
    -DgroupId=com.miklosova.rdftocsvw \
    -DartifactId=RDFtoCSV \
    -Dversion=1.0-SNAPSHOT \
    -Dpackaging=jar

# Build the application using Maven
RUN mvn clean package -DskipTests

# Use an official OpenJDK image as the base image
FROM eclipse-temurin:21-jre-noble
# Set the working directory in the container
WORKDIR /app
# Copy the built JAR file from the previous stage to the container
COPY --from=build /app/target/RDFtoCSVW-0.0.1-SNAPSHOT-exec.jar .
# Set the command to run the application
CMD ["java", "-jar", "RDFtoCSVW-0.0.1-SNAPSHOT-exec.jar"]
