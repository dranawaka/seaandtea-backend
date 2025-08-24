# Railway Deployment Script for Sea & Tea Backend
# This script helps deploy the application to Railway with proper configuration

Write-Host "🚂 Railway Deployment Script for Sea & Tea Backend" -ForegroundColor Green
Write-Host "=================================================" -ForegroundColor Green

# Check if Railway CLI is installed
Write-Host "Checking Railway CLI..." -ForegroundColor Yellow
try {
    $railwayVersion = railway --version
    Write-Host "✅ Railway CLI found: $railwayVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Railway CLI not found. Please install it first:" -ForegroundColor Red
    Write-Host "   npm install -g @railway/cli" -ForegroundColor Yellow
    exit 1
}

# Check if project is linked
Write-Host "Checking project link..." -ForegroundColor Yellow
try {
    $status = railway status
    if ($status -match "No linked project") {
        Write-Host "🔗 Linking project to Railway..." -ForegroundColor Yellow
        railway link
    } else {
        Write-Host "✅ Project already linked" -ForegroundColor Green
    }
} catch {
    Write-Host "⚠️  Could not check project status, continuing..." -ForegroundColor Yellow
}

# Build the application
Write-Host "🔨 Building Spring Boot application..." -ForegroundColor Yellow
try {
    mvn clean package -DskipTests
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build successful" -ForegroundColor Green
    } else {
        Write-Host "❌ Build failed" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Build failed: $_" -ForegroundColor Red
    exit 1
}

# Deploy to Railway
Write-Host "🚀 Deploying to Railway..." -ForegroundColor Yellow
try {
    railway up --detach
    Write-Host "✅ Deployment initiated successfully!" -ForegroundColor Green
} catch {
    Write-Host "❌ Deployment failed: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🎉 Deployment completed!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Go to Railway dashboard to monitor deployment" -ForegroundColor White
Write-Host "2. Database is already configured with Railway internal endpoint" -ForegroundColor Green
Write-Host "3. Set remaining environment variables:" -ForegroundColor White
Write-Host "   - JWT_SECRET (generate a secure random string)" -ForegroundColor White
Write-Host "   - MAIL_USERNAME (your Gmail address)" -ForegroundColor White
Write-Host "   - MAIL_PASSWORD (your Gmail app password)" -ForegroundColor White
Write-Host "   - STRIPE_SECRET_KEY (your Stripe secret key)" -ForegroundColor White
Write-Host "   - STRIPE_PUBLISHABLE_KEY (your Stripe publishable key)" -ForegroundColor White
Write-Host "   - STRIPE_WEBHOOK_SECRET (your Stripe webhook secret)" -ForegroundColor White
Write-Host "   - AWS_ACCESS_KEY (your AWS access key)" -ForegroundColor White
Write-Host "   - AWS_SECRET_KEY (your AWS secret key)" -ForegroundColor White
Write-Host "4. Check deployment logs: railway logs" -ForegroundColor White
Write-Host "5. Open your deployed URL: railway open" -ForegroundColor White
