#!/bin/bash

echo "Starting Sea & Tea Tours Backend..."
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 17 or higher"
    exit 1
fi

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed or not in PATH"
    echo "Please install Maven 3.6 or higher"
    exit 1
fi

echo "Building the application..."
mvn clean install -DskipTests

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo
echo "Starting the application..."
echo "API will be available at: http://localhost:8080/api/v1"
echo "Swagger UI will be available at: http://localhost:8080/swagger-ui/index.html"
echo
echo "Press Ctrl+C to stop the application"
echo

mvn spring-boot:run

