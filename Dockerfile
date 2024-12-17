# Use the official JDK image
FROM openjdk:17-jdk-slim

# Set the working directory
WORKDIR /app

# Copy the fat JAR file into the container
COPY build/libs/*.jar app.jar

# Expose the port your Ktor app uses
EXPOSE 8080

# Run the JAR file
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
