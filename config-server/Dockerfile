FROM openjdk:17-jdk-slim

WORKDIR /app

COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven curl

# Build the application
RUN mvn clean package -DskipTests

# Run the application
EXPOSE 8081
CMD ["java", "-jar", "target/spring-cloud-integration-0.0.1-SNAPSHOT.jar"]