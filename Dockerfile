FROM openjdk:11-jre-slim

# Copy the JAR file from the target directory to the Docker image
COPY target/sportsbot.jar /sportsbot.jar

# Copy the application properties to the Docker image
COPY src/main/resources/application.properties /config/application.properties

# Command to run the application with the specified properties file
CMD ["java", "-jar", "/sportsbot.jar", "--spring.config.location=file:/config/application.properties"]
