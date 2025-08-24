# PowerShell script to create Application Load Balancer for ECS service

Write-Host "🌐 Creating Application Load Balancer for public access..." -ForegroundColor Green

# Create Internet Gateway
Write-Host "🌍 Creating Internet Gateway..." -ForegroundColor Yellow
$internetGateway = aws ec2 create-internet-gateway --region us-east-1 | ConvertFrom-Json
$internetGatewayId = $internetGateway.InternetGateway.InternetGatewayId
Write-Host "✅ Internet Gateway created: $internetGatewayId" -ForegroundColor Green

# Attach Internet Gateway to VPC
Write-Host "🔗 Attaching Internet Gateway to VPC..." -ForegroundColor Yellow
aws ec2 attach-internet-gateway --vpc-id vpc-03c3c2a66a0e4bbb6 --internet-gateway-id $internetGatewayId --region us-east-1
Write-Host "✅ Internet Gateway attached to VPC" -ForegroundColor Green

# Create route table for public subnets
Write-Host "🛣️  Creating route table for public subnets..." -ForegroundColor Yellow
$routeTable = aws ec2 create-route-table --vpc-id vpc-03c3c2a66a0e4bbb6 --region us-east-1 | ConvertFrom-Json
$routeTableId = $routeTable.RouteTable.RouteTableId
Write-Host "✅ Route table created: $routeTableId" -ForegroundColor Green

# Add route to internet gateway
Write-Host "🛣️  Adding route to internet gateway..." -ForegroundColor Yellow
aws ec2 create-route --route-table-id $routeTableId --destination-cidr-block 0.0.0.0/0 --gateway-id $internetGatewayId --region us-east-1
Write-Host "✅ Route added to internet gateway" -ForegroundColor Green

# Associate route table with subnets
Write-Host "🔗 Associating route table with subnets..." -ForegroundColor Yellow
aws ec2 associate-route-table --route-table-id $routeTableId --subnet-id subnet-09972c78d2835e251 --region us-east-1
aws ec2 associate-route-table --route-table-id $routeTableId --subnet-id subnet-02ea7e8189a22c392 --region us-east-1
Write-Host "✅ Route table associated with subnets" -ForegroundColor Green

# Create Application Load Balancer
Write-Host "⚖️  Creating Application Load Balancer..." -ForegroundColor Yellow
$alb = aws elbv2 create-load-balancer --name seaandtea-alb --subnets subnet-09972c78d2835e251 subnet-02ea7e8189a22c392 --security-groups sg-071b9a8c2a54c166e --region us-east-1 | ConvertFrom-Json
$albArn = $alb.LoadBalancers[0].LoadBalancerArn
$albDnsName = $alb.LoadBalancers[0].DNSName
Write-Host "✅ ALB created: $albDnsName" -ForegroundColor Green

# Create target group
Write-Host "🎯 Creating target group..." -ForegroundColor Yellow
$targetGroup = aws elbv2 create-target-group --name seaandtea-tg --protocol HTTP --port 8080 --vpc-id vpc-03c3c2a66a0e4bbb6 --target-type ip --health-check-path /actuator/health --health-check-interval-seconds 30 --healthy-threshold-count 2 --unhealthy-threshold-count 2 --region us-east-1 | ConvertFrom-Json
$targetGroupArn = $targetGroup.TargetGroups[0].TargetGroupArn
Write-Host "✅ Target group created: $targetGroupArn" -ForegroundColor Green

# Create listener
Write-Host "🎧 Creating listener..." -ForegroundColor Yellow
$listener = aws elbv2 create-listener --load-balancer-arn $albArn --protocol HTTP --port 80 --default-actions Type=forward,TargetGroupArn=$targetGroupArn --region us-east-1 | ConvertFrom-Json
Write-Host "✅ Listener created" -ForegroundColor Green

# Update ECS service to use ALB
Write-Host "🔄 Updating ECS service to use ALB..." -ForegroundColor Yellow

# Create service update JSON
$serviceUpdate = @{
    cluster = "seaandtea-cluster"
    service = "seaandtea-backend-service"
    loadBalancers = @(
        @{
            targetGroupArn = $targetGroupArn
            containerName = "seaandtea-backend"
            containerPort = 8080
        }
    )
} | ConvertTo-Json -Depth 10

$serviceUpdate | Out-File -FilePath "update-service-alb.json" -Encoding UTF8

# Update the service
aws ecs update-service --cluster seaandtea-cluster --service seaandtea-backend-service --load-balancers targetGroupArn=$targetGroupArn,containerName=seaandtea-backend,containerPort=8080 --region us-east-1

Write-Host ""
Write-Host "🎉 ALB setup completed successfully!" -ForegroundColor Green
Write-Host "🌐 Your application will be accessible at:" -ForegroundColor Cyan
Write-Host "   http://$albDnsName" -ForegroundColor White
Write-Host "🏥 Health check endpoint:" -ForegroundColor Cyan
Write-Host "   http://$albDnsName/actuator/health" -ForegroundColor White
Write-Host ""
Write-Host "📝 Note: It may take a few minutes for the ALB to become active and register targets." -ForegroundColor Yellow

