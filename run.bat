@echo off
echo Starting Sea & Tea Tours Backend...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 17 or higher
    pause
    exit /b 1
)

REM Check if Maven is installed
mvn -version >nul 2>&1
if errorlevel 1 (
    echo Error: Maven is not installed or not in PATH
    echo Please install Maven 3.6 or higher
    pause
    exit /b 1
)

echo Building the application...
call mvn clean install -DskipTests

if errorlevel 1 (
    echo Error: Build failed
    pause
    exit /b 1
)

echo.
echo Starting the application...
echo API will be available at: http://localhost:8080/api/v1
echo Swagger UI will be available at: http://localhost:8080/swagger-ui/index.html
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run

pause

