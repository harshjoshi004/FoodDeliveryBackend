# Use the official OpenJDK image as the base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the fat JAR file into the container
COPY build/libs/foodapp-all.jar app.jar

# Expose the port your Ktor app uses
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
