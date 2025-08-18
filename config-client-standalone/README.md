# Config Client Standalone Application

A Java 17 standalone application that fetches configuration from a config server and stores it in memory using scheduled jobs.

## Features

- **Scheduled Configuration Refresh**: Automatically fetches configuration at configurable intervals
- **Thread-Safe In-Memory Storage**: Uses ConcurrentHashMap for safe concurrent access
- **Health Monitoring**: Periodic health checks to ensure all configurations are loaded
- **HTTP Client**: Uses Java 11+ HttpClient for reliable config server communication
- **Configurable**: All settings customizable via properties file
- **Comprehensive Logging**: Structured logging with file rotation
- **Graceful Shutdown**: Proper cleanup on application termination

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Access to a configuration server

## Quick Start

1. **Build the application:**
   ```bash
   ./build.sh
   ```

2. **Configure your settings** in `src/main/resources/application.properties`:
   ```properties
   config.server.url=http://your-config-server:8888
   config.applications=myapp,anotherapp
   config.profiles=dev,prod
   ```

3. **Run the application:**
   ```bash
   ./run.sh
   ```

## Configuration

Edit `src/main/resources/application.properties`:

| Property | Description | Default |
|----------|-------------|---------|
| `config.server.url` | Config server base URL | `http://localhost:8888` |
| `config.server.timeout` | HTTP timeout in milliseconds | `5000` |
| `config.applications` | Comma-separated list of applications | `myapp` |
| `config.profiles` | Comma-separated list of profiles | `default` |
| `config.refresh.interval` | Refresh interval in milliseconds | `300000` (5 min) |
| `config.health.check.interval` | Health check interval in milliseconds | `600000` (10 min) |

## Usage

### Programmatic Access

```java
// Get a specific property
String dbUrl = ConfigUtil.getProperty("myapp", "prod", "database.url", "localhost");

// Print all loaded configurations
ConfigUtil.printAllConfigurations();
```

### Monitoring

- Logs are written to both console and `logs/config-client.log`
- Application prints configuration status 10 seconds after startup
- Health checks run every 10 minutes by default

## Project Structure

```
config-client-standalone/
├── pom.xml
├── build.sh
├── run.sh
├── README.md
└── src/main/
    ├── java/com/example/configclient/
    │   ├── ConfigClientApplication.java
    │   ├── config/AppConfig.java
    │   ├── model/Configuration.java
    │   ├── service/ConfigService.java
    │   ├── scheduler/ConfigScheduler.java
    │   └── util/ConfigUtil.java
    └── resources/
        ├── application.properties
        └── logback.xml
```

## Building and Packaging

```bash
# Build only
mvn clean package

# Create distribution ZIP
zip -r config-client-standalone.zip . -x "target/*" "logs/*" "*.zip"
```

## Troubleshooting

1. **Build Fails**: Ensure Java 17+ and Maven are installed and in PATH
2. **Connection Issues**: Verify config server URL and network connectivity
3. **Memory Issues**: Increase JVM memory with `-Xmx512m` or higher
4. **Logging Issues**: Ensure `logs/` directory is writable

## License

This project is provided as-is for educational and development purposes.
