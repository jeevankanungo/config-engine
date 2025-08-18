#!/bin/bash

echo "Building Config Client Library (Java 17)..."
echo "========================================"

# Check Java version
java_version=$(java -version 2>&1 | grep "version" | awk '{print $3}' | tr -d '"')
echo "Java version: $java_version"

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Compile
echo "Compiling..."
mvn compile

# Run tests
echo "Running tests..."
mvn test

# Package
