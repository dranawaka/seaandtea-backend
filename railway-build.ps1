# Railway Build and Deploy Script
# This script ensures the application is built before deployment

Write-Host "🚂 Railway Build and Deploy Script" -ForegroundColor Green
Write-Host "=================================" -ForegroundColor Green

# Check if Java is available
Write-Host "Checking Java installation..." -ForegroundColor Yellow
try {
    $javaVersion = java --version 2>&1
    if ($javaVersion -match "version") {
        Write-Host "✅ Java found" -ForegroundColor Green
    } else {
        Write-Host "❌ Java not found. Railway will install it automatically." -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Java check failed. Railway will handle Java installation." -ForegroundColor Yellow
}

# Check if Maven is available
Write-Host "Checking Maven installation..." -ForegroundColor Yellow
try {
    $mvnVersion = mvn --version 2>&1
    if ($mvnVersion -match "Apache Maven") {
        Write-Host "✅ Maven found" -ForegroundColor Green
    } else {
        Write-Host "❌ Maven not found. Railway will install it automatically." -ForegroundColor Yellow
    }
} catch {
    Write-Host "⚠️  Maven check failed. Railway will handle Maven installation." -ForegroundColor Yellow
}

# Check if target directory exists and has JAR files
Write-Host "Checking build artifacts..." -ForegroundColor Yellow
if (Test-Path "target") {
    $jarFiles = Get-ChildItem "target" -Filter "*.jar" -Recurse
    if ($jarFiles.Count -gt 0) {
        Write-Host "✅ JAR files found in target directory" -ForegroundColor Green
        foreach ($jar in $jarFiles) {
            Write-Host "   - $($jar.Name)" -ForegroundColor White
        }
    } else {
        Write-Host "⚠️  No JAR files found. Railway will build the application." -ForegroundColor Yellow
    }
} else {
    Write-Host "⚠️  Target directory not found. Railway will create it during build." -ForegroundColor Yellow
}

# Check Railway configuration
Write-Host "Checking Railway configuration..." -ForegroundColor Yellow
if (Test-Path "railway.json") {
    Write-Host "✅ railway.json found" -ForegroundColor Green
}
if (Test-Path "railway.toml") {
    Write-Host "✅ railway.toml found" -ForegroundColor Green
}
if (Test-Path "nixpacks.toml") {
    Write-Host "✅ nixpacks.toml found" -ForegroundColor Green
}

Write-Host ""
Write-Host "🚀 Ready to deploy to Railway!" -ForegroundColor Green
Write-Host ""
Write-Host "Configuration Summary:" -ForegroundColor Cyan
Write-Host "- Database: tramway.proxy.rlwy.net:59578" -ForegroundColor White
Write-Host "- Build Command: mvn clean package -DskipTests" -ForegroundColor White
Write-Host "- Start Command: java -jar target/*.jar" -ForegroundColor White
Write-Host "- Health Check: /api/v1/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Run: railway up" -ForegroundColor White
Write-Host "2. Railway will automatically:" -ForegroundColor White
Write-Host "   - Install Java and Maven" -ForegroundColor White
Write-Host "   - Build the application" -ForegroundColor White
Write-Host "   - Create the JAR file" -ForegroundColor White
Write-Host "   - Deploy and start the application" -ForegroundColor White
Write-Host ""
Write-Host "3. Monitor deployment: railway logs" -ForegroundColor White
Write-Host "4. Open application: railway open" -ForegroundColor White


