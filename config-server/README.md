# Spring Boot Cloud Integration

Complete Spring Boot application with enterprise-grade integrations:

## 🎯 Features

- **🗝️ Keycloak** - JWT-based authentication and authorization
- **🚩 Unleash** - Feature flag management and A/B testing  
- **🔐 HashiCorp Vault** - Secure secrets management
- **📂 Spring Cloud Config** - Centralized configuration from GitHub
- **🐳 Docker Compose** - Complete containerized environment
- **🔧 Spring Boot Actuator** - Production-ready monitoring

## 🚀 Quick Start

### 1. Start All Services
```bash
docker-compose up -d
```

### 2. Add Secrets to Vault
```bash
# Add secrets to Vault
docker exec -it vault sh -c "
  export VAULT_ADDR=http://127.0.0.1:8200 && 
  export VAULT_TOKEN=myroot && 
  vault kv put secret/spring-cloud-integration \
    database.username=vault_db_user \
    database.password=vault_secret_password \
    api.keys.weather=vault_weather_key
"
```

### 3. Setup Keycloak (Optional)
1. Open http://localhost:8080
2. Login: admin/admin  
3. Create realm: spring-demo
4. Create client: spring-app
5. Create user for testing and set password.

### 4. Setup Unleash (Optional)
1. Open http://localhost:4242
2. Login: admin/unleash4all
3. Create feature flags:
   - new-profile-feature
   - beta-feature

### 5. Test the Application

```bash
# Public health endpoint
curl http://localhost:8081/demo/public/health

# Configuration endpoint
curl http://localhost:8081/comprehensive-config/all

# Config server endpoints
curl http://localhost:8081/spring-cloud-integration/default
curl http://localhost:8081/foo/default

# Vault test endpoint
curl http://localhost:8081/comprehensive-config/vault-test

# Debug information
curl http://localhost:8081/comprehensive-config/debug
```

## 📁 Configuration Sources

### GitHub Config Server
- Application settings and feature flags
- Environment-specific configurations
- Update `application.yml` with your GitHub repo URL

### HashiCorp Vault
- Database credentials and API keys
- Sensitive configuration data
- Auto-loaded when `vault-enabled` profile is active

## 🔧 Configuration

### Enable Integrations

Edit `application.yml` to enable features:

```yaml
keycloak:
  enabled: true  # Enable JWT authentication

unleash:
  enabled: true  # Enable feature flags

vault:
  enabled: true  # Enable secrets from Vault
```

### GitHub Repository Setup

1. Create a GitHub repository: `spring-cloud-config-repo`
2. Add sample config files from `sample-github-config/` directory
3. Update `spring.cloud.config.server.git.uri` in `application.yml`

## 🏗️ Architecture

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   GitHub    │───▶│   Config    │───▶│   Spring    │
│ Repository  │    │   Server    │    │  Boot App   │
└─────────────┘    └─────────────┘    └─────────────┘
                                              │
┌─────────────┐    ┌─────────────┐           │
│    Vault    │───▶│  Secrets    │──────────▶│
│  (Secrets)  │    │ Management  │           │
└─────────────┘    └─────────────┘           │
                                              │
┌─────────────┐    ┌─────────────┐           │
│  Keycloak   │───▶│     JWT     │──────────▶│
│   (Auth)    │    │Authentication│           │
└─────────────┘    └─────────────┘           │
                                              │
┌─────────────┐    ┌─────────────┐           │
│   Unleash   │───▶│  Feature    │──────────▶│
│  (Flags)    │    │   Flags     │           │
└─────────────┘    └─────────────┘           ▼
```

## 📊 Monitoring

Spring Boot Actuator endpoints available at:

- Health: http://localhost:8081/actuator/health
- Environment: http://localhost:8081/actuator/env  
- Configuration: http://localhost:8081/actuator/configprops
- Refresh: http://localhost:8081/actuator/refresh (POST)

## 🔒 Security

- JWT token validation via Keycloak
- Secrets stored securely in Vault
- Configurable security profiles
- Production-ready security defaults

## 🚀 Production Deployment

1. Use external databases (not H2)
2. Configure proper SSL/TLS certificates
3. Set up monitoring and alerting
4. Use production-grade tokens and secrets
5. Enable proper logging and metrics

## 📝 Development

### Local Development
```bash
# Start infrastructure only
docker-compose up -d vault keycloak unleash postgres

# Run Spring Boot locally
SPRING_PROFILES_ACTIVE=dev,vault-enabled mvn spring-boot:run   -Dlogging.level.org.springframework.cloud.bootstrap=DEBUG   -Dlogging.level.org.springframework.cloud.config=DEBUG -Dspring.cloud.config.enabled=false -Dspring.cloud.config.uri=http://localhost:8081 -Dspring.application.name=spring-cloud-integration

Once app is running, manually refresh configs

curl -X POST http://localhost:8081/actuator/refresh
```

### Building
```bash
# Build Docker image
docker build -t spring-cloud-integration .

# Build with Maven
mvn clean package
```

## 🐛 Troubleshooting

### Common Issues

1. **Config Server not loading GitHub configs**
   - Check GitHub repository URL in `application.yml`
   - Verify repository is public or credentials are set

2. **Vault secrets not loading**
   - Ensure `vault-enabled` profile is active
   - Verify secrets exist: `vault kv get secret/spring-cloud-integration`

3. **Unleash connection errors**
   - Check if Unleash service is running: `docker-compose logs unleash`
   - Verify API token configuration

4. **Keycloak authentication issues**
   - Create `spring-demo` realm and `spring-app` client
   - Check JWT issuer URI configuration

### Debug Commands

```bash
# Check service logs
docker-compose logs [service-name]

# Check application configuration
curl http://localhost:8081/comprehensive-config/debug

# Test individual integrations
curl http://localhost:8081/comprehensive-config/vault-test
```

## 📚 Additional Resources

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [HashiCorp Vault Documentation](https://www.vaultproject.io/docs)
- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Unleash Documentation](https://docs.getunleash.io/)

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request