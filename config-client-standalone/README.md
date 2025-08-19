# Config Client Auto-Starting Library

A Java 17 standalone application that automatically starts when loaded as a library, fetching configuration from a config server and storing it in memory using scheduled jobs.

## ✨ Key Features

- **🚀 Auto-Start**: Automatically initializes when added to classpath (perfect for Spring Boot)
- **🔄 Scheduled Refresh**: Automatically fetches configuration at configurable intervals
- **🧵 Thread-Safe**: Uses ConcurrentHashMap for safe concurrent access
- **🔍 Smart Change Detection**: Uses Google Guava MapDifference for precise change detection
- **💾 In-Memory Storage**: Fast access to configurations
- **📊 Health Monitoring**: Periodic health checks
- **⚙️ Highly Configurable**: All settings customizable via properties
- **📝 Comprehensive Logging**: Structured logging with detailed change tracking

## 🚀 Quick Start for Spring Boot Integration

### 1. Add as Dependency

Build this library and add the JAR to your Spring Boot application's classpath:

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>config-client-standalone</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. Configure (Optional)

Add `application.properties` to your Spring Boot app's resources:

```properties
config.server.url=http://your-config-server:8888
config.applications=your-app,another-app
config.profiles=dev,prod
config.refresh.interval=300000
```

### 3. Use in Your Code

```java
import com.example.configclient.util.ConfigClientLibrary;

@Service
public class MyService {
    
    public void someMethod() {
        // Get configuration property
        String dbUrl = ConfigClientLibrary.getProperty("myapp", "prod", "database.url", "localhost");
        
        // Check if config client is running
        if (ConfigClientLibrary.isRunning()) {
            System.out.println("Config client is active!");
        }
        
        // Force refresh configurations
        ConfigClientLibrary.refreshConfigurations();
    }
}
```

## 📋 How Auto-Start Works

The library uses a **static initializer** in `ConfigClientManager` that runs when the class is first loaded:

1. When your Spring Boot app starts and loads classes from this JAR
2. The `ConfigClientManager` class is loaded
3. Static initializer block executes automatically
4. Config client starts fetching configurations in the background
5. Your application can immediately access configurations via `ConfigClientLibrary`

## 🔧 Configuration Options

| Property | Description | Default |
|----------|-------------|---------|
| `config.server.url` | Config server base URL | `http://localhost:8888` |
| `config.server.timeout` | HTTP timeout (ms) | `5000` |
| `config.applications` | Applications to monitor | `myapp` |
| `config.profiles` | Profiles to monitor | `default` |
| `config.refresh.interval` | Refresh interval (ms) | `300000` |
| `config.health.check.interval` | Health check interval (ms) | `600000` |
| `config.client.autostart` | Enable auto-start | `true` |

## 🎯 Guava MapDifference Integration

The library now uses Google Guava's `MapDifference` for precise change detection:

```
Configuration refreshed for myapp-prod with changes: 
Added: [new.property]; Modified: [database.timeout]; Removed: [old.setting];
```

This provides detailed insights into exactly what changed between configuration versions.

## 📖 API Reference

### ConfigClientLibrary Methods

```java
// Get property with default
String value = ConfigClientLibrary.getProperty("app", "prod", "key", "default");

// Get complete configuration
Configuration config = ConfigClientLibrary.getConfiguration("app", "prod");

// Get all configurations
Map<String, Configuration> all = ConfigClientLibrary.getAllConfigurations();

// Force refresh
ConfigClientLibrary.refreshConfigurations();

// Check status
boolean running = ConfigClientLibrary.isRunning();
int count = ConfigClientLibrary.getConfigurationCount();

// Debug output
ConfigClientLibrary.printConfigurations();
```

## 🏗️ Building and Packaging

```bash
# Build the library
./build.sh

# For use as dependency in other projects
mvn install
```

## 🔧 Standalone Usage

You can still run it standalone:

```bash
java -jar target/config-client-standalone-1.0.0-shaded.jar
```

## 🐛 Troubleshooting

### Auto-Start Not Working
- Ensure JAR is in classpath
- Check `config.client.autostart=true` in properties
- Verify logs for initialization messages

### Configuration Not Loading
- Check config server URL and connectivity
- Verify application and profile names
- Review logs for HTTP errors

### Spring Boot Integration Issues
- Ensure proper dependency configuration
- Check for classpath conflicts
- Verify property file location

## 📁 Project Structure

```
config-client-standalone/
├── src/main/java/com/example/configclient/
│   ├── ConfigClientApplication.java      # Standalone main class
│   ├── ConfigClientManager.java          # Auto-starting singleton
│   ├── config/AppConfig.java             # Configuration loader
│   ├── model/Configuration.java          # Data model
│   ├── service/ConfigService.java        # HTTP client service
│   ├── scheduler/ConfigScheduler.java    # Guava-enhanced scheduler
│   └── util/
│       ├── ConfigUtil.java               # Utility functions
│       └── ConfigClientLibrary.java      # Spring Boot API
└── src/main/resources/
    ├── application.properties             # Configuration
    └── logback.xml                       # Logging config
```

## 🎉 Benefits for Spring Boot Applications

1. **Zero Configuration**: Just add JAR to classpath
2. **Non-Intrusive**: Runs in background threads
3. **Production Ready**: Comprehensive error handling and logging
4. **Performance**: Fast in-memory access to configurations
5. **Detailed Monitoring**: Precise change detection and health checks
6. **Easy Integration**: Simple static API for configuration access

Perfect for microservices that need dynamic configuration management! 🚀
