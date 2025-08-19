#!/bin/bash
echo "Building Config Client Auto-Starting Library..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven and try again"
    exit 1
fi

# Check Java version
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "Error: Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "Java version check passed: $JAVA_VERSION"
echo "Starting Maven build..."

mvn clean package

if [ $? -eq 0 ]; then
    echo "✅ Build completed successfully!"
    echo "JAR file: target/config-client-standalone-1.0.0-shaded.jar"
    echo ""
    echo "🚀 Usage Options:"
    echo "1. Standalone: ./run.sh"
    echo "2. Library: Add JAR to your Spring Boot classpath"
    echo "3. Maven install: mvn install (for local repository)"
    echo ""
    echo "📚 Library Integration:"
    echo "   import com.example.configclient.util.ConfigClientLibrary;"
    echo "   String value = ConfigClientLibrary.getProperty(\"app\", \"prod\", \"key\");"
else
    echo "❌ Build failed!"
    exit 1
fi
