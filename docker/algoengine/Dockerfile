# Use the eclipse temurin alpine JRE distribution (Java 21)
FROM eclipse-temurin:21-jre-alpine

# Create a non-root user and group
RUN addgroup -S spring && adduser -S spring -G spring

# Copy the Spring Boot JAR to the /opt directory
COPY /target/algo-engine-*.jar /opt/algo.jar

# Set the non-root user as the default user
USER spring:spring

# Set the working directory
WORKDIR /opt

# Define the entry point to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "algo.jar"]

# Expose the port to the external world
EXPOSE 8080