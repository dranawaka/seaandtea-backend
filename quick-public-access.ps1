# Quick script to enable public access to ECS tasks

Write-Host "🚀 Quick fix for public access..." -ForegroundColor Green

# First, let's check the current ECS service status
Write-Host "📊 Checking current ECS service status..." -ForegroundColor Yellow
aws ecs describe-services --cluster seaandtea-cluster --services seaandtea-backend-service --region us-east-1 --query "services[0].{Status:status,Running:runningCount,Pending:pendingCount}" --output table

Write-Host ""
Write-Host "🔍 Current networking setup:" -ForegroundColor Cyan
Write-Host "   VPC: vpc-03c3c2a66a0e4bbb6" -ForegroundColor White
Write-Host "   Subnets: Private subnets (no internet gateway)" -ForegroundColor White
Write-Host "   Security Group: sg-071b9a8c2a54c166e" -ForegroundColor White

Write-Host ""
Write-Host "💡 To enable public access, you need to:" -ForegroundColor Yellow
Write-Host "   1. Create an Internet Gateway" -ForegroundColor White
Write-Host "   2. Attach it to your VPC" -ForegroundColor White
Write-Host "   3. Create a route table with internet access" -ForegroundColor White
Write-Host "   4. Associate the route table with your subnets" -ForegroundColor White
Write-Host "   5. Add an Application Load Balancer" -ForegroundColor White

Write-Host ""
Write-Host "🚀 Run the ALB setup script for the complete solution:" -ForegroundColor Green
Write-Host "   .\create-alb.ps1" -ForegroundColor Cyan

Write-Host ""
Write-Host "📝 Alternative: You can also access your app through:" -ForegroundColor Yellow
Write-Host "   - AWS Systems Manager Session Manager" -ForegroundColor White
Write-Host "   - AWS Cloud9 IDE" -ForegroundColor White
Write-Host "   - VPN connection to your VPC" -ForegroundColor White
