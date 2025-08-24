# Simplified ALB setup script

Write-Host "🌐 Setting up Application Load Balancer..." -ForegroundColor Green

try {
    # Step 1: Create Internet Gateway
    Write-Host "🌍 Creating Internet Gateway..." -ForegroundColor Yellow
    $igw = aws ec2 create-internet-gateway --region us-east-1
    Write-Host "✅ Internet Gateway created" -ForegroundColor Green
    
    # Extract the Internet Gateway ID
    $igwId = ($igw | ConvertFrom-Json).InternetGateway.InternetGatewayId
    Write-Host "   ID: $igwId" -ForegroundColor Cyan
    
    # Step 2: Attach to VPC
    Write-Host "🔗 Attaching Internet Gateway to VPC..." -ForegroundColor Yellow
    aws ec2 attach-internet-gateway --vpc-id vpc-03c3c2a66a0e4bbb6 --internet-gateway-id $igwId --region us-east-1
    Write-Host "✅ Internet Gateway attached" -ForegroundColor Green
    
    # Step 3: Create Route Table
    Write-Host "🛣️  Creating Route Table..." -ForegroundColor Yellow
    $rt = aws ec2 create-route-table --vpc-id vpc-03c3c2a66a0e4bbb6 --region us-east-1
    $rtId = ($rt | ConvertFrom-Json).RouteTable.RouteTableId
    Write-Host "✅ Route Table created: $rtId" -ForegroundColor Green
    
    # Step 4: Add Internet Route
    Write-Host "🛣️  Adding internet route..." -ForegroundColor Yellow
    aws ec2 create-route --route-table-id $rtId --destination-cidr-block 0.0.0.0/0 --gateway-id $igwId --region us-east-1
    Write-Host "✅ Internet route added" -ForegroundColor Green
    
    # Step 5: Associate with Subnets
    Write-Host "🔗 Associating route table with subnets..." -ForegroundColor Yellow
    aws ec2 associate-route-table --route-table-id $rtId --subnet-id subnet-09972c78d2835e251 --region us-east-1
    aws ec2 associate-route-table --route-table-id $rtId --subnet-id subnet-02ea7e8189a22c392 --region us-east-1
    Write-Host "✅ Route table associated" -ForegroundColor Green
    
    # Step 6: Create ALB
    Write-Host "⚖️  Creating Application Load Balancer..." -ForegroundColor Yellow
    $alb = aws elbv2 create-load-balancer --name seaandtea-alb --subnets subnet-09972c78d2835e251 subnet-02ea7e8189a22c392 --security-groups sg-071b9a8c2a54c166e --region us-east-1
    $albArn = ($alb | ConvertFrom-Json).LoadBalancers[0].LoadBalancerArn
    $albDns = ($alb | ConvertFrom-Json).LoadBalancers[0].DNSName
    Write-Host "✅ ALB created: $albDns" -ForegroundColor Green
    
    # Step 7: Create Target Group
    Write-Host "🎯 Creating Target Group..." -ForegroundColor Yellow
    $tg = aws elbv2 create-target-group --name seaandtea-tg --protocol HTTP --port 8080 --vpc-id vpc-03c3c2a66a0e4bbb6 --target-type ip --health-check-path /actuator/health --region us-east-1
    $tgArn = ($tg | ConvertFrom-Json).TargetGroups[0].TargetGroupArn
    Write-Host "✅ Target Group created: $tgArn" -ForegroundColor Green
    
    # Step 8: Create Listener
    Write-Host "🎧 Creating Listener..." -ForegroundColor Yellow
    aws elbv2 create-listener --load-balancer-arn $albArn --protocol HTTP --port 80 --default-actions Type=forward,TargetGroupArn=$tgArn --region us-east-1
    Write-Host "✅ Listener created" -ForegroundColor Green
    
    # Step 9: Update ECS Service
    Write-Host "🔄 Updating ECS Service..." -ForegroundColor Yellow
    aws ecs update-service --cluster seaandtea-cluster --service seaandtea-backend-service --load-balancers targetGroupArn=$tgArn,containerName=seaandtea-backend,containerPort=8080 --region us-east-1
    Write-Host "✅ ECS Service updated" -ForegroundColor Green
    
    Write-Host ""
    Write-Host "🎉 ALB Setup Complete!" -ForegroundColor Green
    Write-Host "🌐 Your application will be accessible at:" -ForegroundColor Cyan
    Write-Host "   http://$albDns" -ForegroundColor White
    Write-Host "🏥 Health check: http://$albDns/actuator/health" -ForegroundColor White
    
} catch {
    Write-Host "❌ Error during ALB setup: $_" -ForegroundColor Red
}

