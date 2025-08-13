# Sample GitHub Configuration Files

These are example configuration files for your Spring Cloud Config repository.

## Setup Instructions

1. Create a new GitHub repository called `spring-cloud-config-repo`
2. Upload these files to the root of your repository
3. Update the `spring.cloud.config.server.git.uri` in your `application.yml` to point to your repository
4. Restart your Spring Boot application

## File Structure

- `spring-cloud-integration.yml` - Default configuration
- `spring-cloud-integration-dev.yml` - Development environment  
- `spring-cloud-integration-prod.yml` - Production environment

## Usage

The Spring Cloud Config server will serve these configurations at:

- `http://localhost:8081/spring-cloud-integration/default`
- `http://localhost:8081/spring-cloud-integration/dev`  
- `http://localhost:8081/spring-cloud-integration/prod`

Your Spring Boot application will automatically load the appropriate configuration based on the active profile.