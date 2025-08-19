// Example: How to use this library in a Spring Boot application

import com.example.configclient.util.ConfigClientLibrary;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.util.Map;

@SpringBootApplication
@RestController
public class SpringBootExample {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExample.class, args);
    }

    @PostConstruct
    public void init() {
        // Config client auto-started when this JAR was loaded!
        System.out.println("Config Client Status: " + 
            (ConfigClientLibrary.isRunning() ? "RUNNING" : "STOPPED"));
        
        // Print initial configurations after a delay
        new Thread(() -> {
            try {
                Thread.sleep(5000); // Wait for initial load
                System.out.println("=== Initial Configurations ===");
                ConfigClientLibrary.printConfigurations();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @GetMapping("/config/status")
    public Map<String, Object> getConfigStatus() {
        return Map.of(
            "running", ConfigClientLibrary.isRunning(),
            "configCount", ConfigClientLibrary.getConfigurationCount(),
            "configurations", ConfigClientLibrary.getAllConfigurations()
        );
    }

    @GetMapping("/config/property")
    public String getProperty() {
        // Example: Get a property with fallback
        return ConfigClientLibrary.getProperty("myapp", "prod", "database.url", "localhost:5432");
    }

    @GetMapping("/config/refresh")
    public String refreshConfig() {
        ConfigClientLibrary.refreshConfigurations();
        return "Configuration refresh triggered";
    }
}
