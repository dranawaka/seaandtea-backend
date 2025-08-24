# PowerShell script to create IAM roles for ECS deployment

Write-Host "🔐 Creating IAM roles for ECS deployment..." -ForegroundColor Green

# Create ECS Task Execution Role
Write-Host "📝 Creating ECS Task Execution Role..." -ForegroundColor Yellow

$trustPolicy = @{
    Version = "2012-10-17"
    Statement = @(
        @{
            Effect = "Allow"
            Principal = @{
                Service = "ecs-tasks.amazonaws.com"
            }
            Action = "sts:AssumeRole"
        }
    )
} | ConvertTo-Json -Depth 10

# Create the role
aws iam create-role --role-name ecsTaskExecutionRole --assume-role-policy-document $trustPolicy

# Attach the required policy
aws iam attach-role-policy --role-name ecsTaskExecutionRole --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

# Create ECS Task Role
Write-Host "📝 Creating ECS Task Role..." -ForegroundColor Yellow

# Create the role
aws iam create-role --role-name ecsTaskRole --assume-role-policy-document $trustPolicy

# Create custom policy for the task role
$taskPolicy = @{
    Version = "2012-10-17"
    Statement = @(
        @{
            Effect = "Allow"
            Action = @(
                "logs:CreateLogGroup",
                "logs:CreateLogStream",
                "logs:PutLogEvents"
            )
            Resource = "*"
        }
    )
} | ConvertTo-Json -Depth 10

# Create the policy
aws iam create-policy --policy-name ECSTaskRolePolicy --policy-document $taskPolicy

# Attach the policy to the role
aws iam attach-role-policy --role-name ecsTaskRole --policy-arn arn:aws:iam::499055708506:policy/ECSTaskRolePolicy

Write-Host "✅ IAM roles created successfully!" -ForegroundColor Green
Write-Host "📋 Roles created:" -ForegroundColor Cyan
Write-Host "   - ecsTaskExecutionRole" -ForegroundColor White
Write-Host "   - ecsTaskRole" -ForegroundColor White

