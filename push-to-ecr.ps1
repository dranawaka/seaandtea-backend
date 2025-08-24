# PowerShell script to push Docker image to Amazon ECR
# Make sure you have AWS CLI configured and Docker running

param(
    [Parameter(Mandatory=$true)]
    [string]$ECRRepositoryName,
    
    [Parameter(Mandatory=$true)]
    [string]$AWSRegion,
    
    [Parameter(Mandatory=$false)]
    [string]$ImageTag = "latest"
)

Write-Host "🚀 Starting ECR push process..." -ForegroundColor Green

# Check if Docker is running
try {
    docker version | Out-Null
    Write-Host "✅ Docker is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Docker is not running. Please start Docker Desktop first." -ForegroundColor Red
    exit 1
}

# Check AWS CLI configuration
try {
    $awsAccountId = aws sts get-caller-identity --query Account --output text 2>$null
    if ($awsAccountId) {
        Write-Host "✅ AWS CLI configured. Account ID: $awsAccountId" -ForegroundColor Green
    } else {
        Write-Host "❌ AWS CLI not configured. Please run 'aws configure' first." -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ AWS CLI not configured. Please run 'aws configure' first." -ForegroundColor Red
    exit 1
}

# Build the Docker image
Write-Host "🔨 Building Docker image..." -ForegroundColor Yellow
docker build -t "seaandtea-backend:$ImageTag" .

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker build failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ Docker image built successfully" -ForegroundColor Green

# Get ECR login token
Write-Host "🔐 Getting ECR login token..." -ForegroundColor Yellow
aws ecr get-login-password --region $AWSRegion | docker login --username AWS --password-stdin $awsAccountId.dkr.ecr.$AWSRegion.amazonaws.com

if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ ECR login failed" -ForegroundColor Red
    exit 1
}

Write-Host "✅ ECR login successful" -ForegroundColor Green

# Create ECR repository if it doesn't exist
Write-Host "🏗️  Creating ECR repository if it doesn't exist..." -ForegroundColor Yellow
aws ecr describe-repositories --repository-names $ECRRepositoryName --region $AWSRegion 2>$null
if ($LASTEXITCODE -ne 0) {
    Write-Host "📦 Creating ECR repository: $ECRRepositoryName" -ForegroundColor Yellow
    aws ecr create-repository --repository-name $ECRRepositoryName --region $AWSRegion
    Write-Host "✅ ECR repository created" -ForegroundColor Green
} else {
    Write-Host "✅ ECR repository already exists" -ForegroundColor Green
}

# Tag the image for ECR
$ecrImageUri = "$awsAccountId.dkr.ecr.$AWSRegion.amazonaws.com/$ECRRepositoryName" + ":$ImageTag"
Write-Host "🏷️  Tagging image for ECR: $ecrImageUri" -ForegroundColor Yellow
docker tag "seaandtea-backend:$ImageTag" $ecrImageUri

# Push the image to ECR
Write-Host "📤 Pushing image to ECR..." -ForegroundColor Yellow
docker push $ecrImageUri

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Image pushed successfully to ECR!" -ForegroundColor Green
    Write-Host "📍 Image URI: $ecrImageUri" -ForegroundColor Cyan
} else {
    Write-Host "❌ Failed to push image to ECR" -ForegroundColor Red
    exit 1
}

Write-Host "🎉 ECR push process completed successfully!" -ForegroundColor Green
