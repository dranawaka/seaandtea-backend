@echo off
REM Batch script to push Docker image to Amazon ECR
REM Usage: push-to-ecr.bat <ECR_REPOSITORY_NAME> <AWS_REGION> [IMAGE_TAG]

if "%1"=="" (
    echo Usage: push-to-ecr.bat ^<ECR_REPOSITORY_NAME^> ^<AWS_REGION^> [IMAGE_TAG]
    echo Example: push-to-ecr.bat seaandtea-backend us-east-1 latest
    exit /b 1
)

if "%2"=="" (
    echo Usage: push-to-ecr.bat ^<ECR_REPOSITORY_NAME^> ^<AWS_REGION^> [IMAGE_TAG]
    echo Example: push-to-ecr.bat seaandtea-backend us-east-1 latest
    exit /b 1
)

set ECR_REPO_NAME=%1
set AWS_REGION=%2
set IMAGE_TAG=%3
if "%IMAGE_TAG%"=="" set IMAGE_TAG=latest

echo 🚀 Starting ECR push process...
echo 📦 Repository: %ECR_REPO_NAME%
echo 🌍 Region: %AWS_REGION%
echo 🏷️  Tag: %IMAGE_TAG%

REM Check if Docker is running
docker version >nul 2>&1
if errorlevel 1 (
    echo ❌ Docker is not running. Please start Docker Desktop first.
    exit /b 1
)
echo ✅ Docker is running

REM Check AWS CLI configuration
for /f "tokens=*" %%i in ('aws sts get-caller-identity --query Account --output text 2^>nul') do set AWS_ACCOUNT_ID=%%i
if "%AWS_ACCOUNT_ID%"=="" (
    echo ❌ AWS CLI not configured. Please run 'aws configure' first.
    exit /b 1
)
echo ✅ AWS CLI configured. Account ID: %AWS_ACCOUNT_ID%

REM Build the Docker image
echo 🔨 Building Docker image...
docker build -t seaandtea-backend:%IMAGE_TAG% .
if errorlevel 1 (
    echo ❌ Docker build failed
    exit /b 1
)
echo ✅ Docker image built successfully

REM Get ECR login token
echo 🔐 Getting ECR login token...
aws ecr get-login-password --region %AWS_REGION% | docker login --username AWS --password-stdin %AWS_ACCOUNT_ID%.dkr.ecr.%AWS_REGION%.amazonaws.com
if errorlevel 1 (
    echo ❌ ECR login failed
    exit /b 1
)
echo ✅ ECR login successful

REM Create ECR repository if it doesn't exist
echo 🏗️  Creating ECR repository if it doesn't exist...
aws ecr describe-repositories --repository-names %ECR_REPO_NAME% --region %AWS_REGION% >nul 2>&1
if errorlevel 1 (
    echo 📦 Creating ECR repository: %ECR_REPO_NAME%
    aws ecr create-repository --repository-name %ECR_REPO_NAME% --region %AWS_REGION%
    echo ✅ ECR repository created
) else (
    echo ✅ ECR repository already exists
)

REM Tag the image for ECR
set ECR_IMAGE_URI=%AWS_ACCOUNT_ID%.dkr.ecr.%AWS_REGION%.amazonaws.com/%ECR_REPO_NAME%:%IMAGE_TAG%
echo 🏷️  Tagging image for ECR: %ECR_IMAGE_URI%
docker tag seaandtea-backend:%IMAGE_TAG% %ECR_IMAGE_URI%

REM Push the image to ECR
echo 📤 Pushing image to ECR...
docker push %ECR_IMAGE_URI%
if errorlevel 1 (
    echo ❌ Failed to push image to ECR
    exit /b 1
)

echo ✅ Image pushed successfully to ECR!
echo 📍 Image URI: %ECR_IMAGE_URI%
echo 🎉 ECR push process completed successfully!

