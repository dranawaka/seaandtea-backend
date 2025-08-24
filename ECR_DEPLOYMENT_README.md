# Deploying to Amazon ECR

This guide will help you push your Sea & Tea Tours backend Docker image to Amazon Elastic Container Registry (ECR).

## Prerequisites

1. **AWS Account**: You need an active AWS account
2. **AWS CLI**: Installed and configured (already done in this session)
3. **Docker**: Running locally
4. **IAM Permissions**: Your AWS user needs ECR permissions

## Required IAM Permissions

Your AWS user/role needs the following permissions for ECR:

```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "ecr:GetAuthorizationToken",
                "ecr:BatchCheckLayerAvailability",
                "ecr:GetDownloadUrlForLayer",
                "ecr:BatchGetImage",
                "ecr:InitiateLayerUpload",
                "ecr:UploadLayerPart",
                "ecr:CompleteLayerUpload",
                "ecr:PutImage",
                "ecr:CreateRepository",
                "ecr:DescribeRepositories"
            ],
            "Resource": "*"
        }
    ]
}
```

## Quick Start

### Option 1: Using PowerShell Script (Recommended)

```powershell
# Make sure you're in the project directory
cd seaandtea-backend

# Run the script with your ECR repository name and AWS region
.\push-to-ecr.ps1 -ECRRepositoryName "seaandtea-backend" -AWSRegion "us-east-1" -ImageTag "latest"
```

### Option 2: Using Batch File

```cmd
# Make sure you're in the project directory
cd seaandtea-backend

# Run the batch file with your ECR repository name and AWS region
push-to-ecr.bat seaandtea-backend us-east-1 latest
```

### Option 3: Manual Commands

If you prefer to run commands manually:

```bash
# 1. Configure AWS CLI (if not already done)
aws configure

# 2. Build the Docker image
docker build -t seaandtea-backend:latest .

# 3. Get ECR login token
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com

# 4. Create ECR repository (if it doesn't exist)
aws ecr create-repository --repository-name seaandtea-backend --region us-east-1

# 5. Tag the image for ECR
docker tag seaandtea-backend:latest YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/seaandtea-backend:latest

# 6. Push to ECR
docker push YOUR_ACCOUNT_ID.dkr.ecr.us-east-1.amazonaws.com/seaandtea-backend:latest
```

## Configuration

### AWS CLI Configuration

If you haven't configured AWS CLI yet:

```bash
aws configure
```

You'll be prompted for:
- **AWS Access Key ID**: Your AWS access key
- **AWS Secret Access Key**: Your AWS secret key
- **Default region name**: Your preferred AWS region (e.g., us-east-1)
- **Default output format**: json (recommended)

### Environment Variables

You can also set environment variables:

```bash
# Windows PowerShell
$env:AWS_ACCESS_KEY_ID="your_access_key"
$env:AWS_SECRET_ACCESS_KEY="your_secret_key"
$env:AWS_DEFAULT_REGION="us-east-1"

# Windows CMD
set AWS_ACCESS_KEY_ID=your_access_key
set AWS_SECRET_ACCESS_KEY=your_secret_key
set AWS_DEFAULT_REGION=us-east-1
```

## What the Scripts Do

1. **Check Prerequisites**: Verify Docker is running and AWS CLI is configured
2. **Build Image**: Build the Docker image locally
3. **ECR Authentication**: Get and use ECR login token
4. **Repository Creation**: Create ECR repository if it doesn't exist
5. **Image Tagging**: Tag the local image for ECR
6. **Push to ECR**: Upload the image to Amazon ECR

## Troubleshooting

### Common Issues

1. **Docker not running**
   - Start Docker Desktop
   - Wait for Docker to fully initialize

2. **AWS CLI not configured**
   - Run `aws configure`
   - Verify your credentials are correct

3. **Permission denied**
   - Check your IAM permissions
   - Ensure you have ECR access

4. **Repository already exists**
   - This is normal, the script will skip creation

5. **Image push fails**
   - Check your internet connection
   - Verify ECR repository exists
   - Check AWS credentials

### Debugging

Enable verbose output:

```bash
# AWS CLI verbose
aws --debug ecr get-login-password --region us-east-1

# Docker verbose
docker --debug push your-image-uri
```

## Next Steps

After successfully pushing to ECR, you can:

1. **Deploy to ECS**: Use the image in Amazon ECS
2. **Deploy to EKS**: Use the image in Amazon EKS
3. **Deploy to App Runner**: Use the image in AWS App Runner
4. **Deploy to Lambda**: Use the image in AWS Lambda (if compatible)

## Security Best Practices

1. **Use IAM Roles**: Prefer IAM roles over access keys when possible
2. **Rotate Credentials**: Regularly rotate your AWS access keys
3. **Least Privilege**: Grant only necessary permissions
4. **Image Scanning**: Enable ECR image scanning for vulnerabilities
5. **Private Repositories**: Use private ECR repositories for production

## Cost Considerations

- **ECR Storage**: $0.10 per GB-month for standard storage
- **Data Transfer**: Free within the same region
- **Repository**: Free (no additional charges for repositories)

## Support

If you encounter issues:

1. Check the troubleshooting section above
2. Verify your AWS credentials and permissions
3. Check AWS CloudTrail for detailed error logs
4. Consult AWS ECR documentation

