#!/bin/bash

JAR_FILE="target/config-client-standalone-1.0.0-shaded.jar"

echo "Config Client Auto-Starting Library - Standalone Mode"
echo "====================================================="

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found: $JAR_FILE"
    echo "Building the application first..."
    ./build.sh
    
    if [ ! -f "$JAR_FILE" ]; then
        echo "Build failed. Cannot start application."
        exit 1
    fi
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

echo "🚀 Starting Config Client Application in standalone mode..."
echo "📍 JAR: $JAR_FILE"
echo "☕ Java Version: $JAVA_VERSION"
echo "📝 Config will auto-load from application.properties"
echo ""
echo "💡 Note: When used as library, auto-starts automatically!"
echo "Press Ctrl+C to stop the application"
echo "====================================================="

# Run the application
java -jar "$JAR_FILE"
