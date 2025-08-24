@echo off
echo 🌐 Setting up Application Load Balancer...

echo 🌍 Creating Internet Gateway...
for /f "tokens=*" %%i in ('aws ec2 create-internet-gateway --region us-east-1 --output json') do set IGW_JSON=%%i
echo ✅ Internet Gateway created

echo 🔗 Attaching Internet Gateway to VPC...
aws ec2 attach-internet-gateway --vpc-id vpc-03c3c2a66a0e4bbb6 --internet-gateway-id %IGW_ID% --region us-east-1
echo ✅ Internet Gateway attached

echo 🛣️  Creating Route Table...
for /f "tokens=*" %%i in ('aws ec2 create-route-table --vpc-id vpc-03c3c2a66a0e4bbb6 --region us-east-1 --output json') do set RT_JSON=%%i
echo ✅ Route Table created

echo 🛣️  Adding internet route...
aws ec2 create-route --route-table-id %RT_ID% --destination-cidr-block 0.0.0.0/0 --gateway-id %IGW_ID% --region us-east-1
echo ✅ Internet route added

echo 🔗 Associating route table with subnets...
aws ec2 associate-route-table --route-table-id %RT_ID% --subnet-id subnet-09972c78d2835e251 --region us-east-1
aws ec2 associate-route-table --route-table-id %RT_ID% --subnet-id subnet-02ea7e8189a22c392 --region us-east-1
echo ✅ Route table associated

echo ⚖️  Creating Application Load Balancer...
for /f "tokens=*" %%i in ('aws elbv2 create-load-balancer --name seaandtea-alb --subnets subnet-09972c78d2835e251 subnet-02ea7e8189a22c392 --security-groups sg-071b9a8c2a54c166e --region us-east-1 --output json') do set ALB_JSON=%%i
echo ✅ ALB created

echo 🎯 Creating Target Group...
for /f "tokens=*" %%i in ('aws elbv2 create-target-group --name seaandtea-tg --protocol HTTP --port 8080 --vpc-id vpc-03c3c2a66a0e4bbb6 --target-type ip --health-check-path /actuator/health --region us-east-1 --output json') do set TG_JSON=%%i
echo ✅ Target Group created

echo 🎧 Creating Listener...
aws elbv2 create-listener --load-balancer-arn %ALB_ARN% --protocol HTTP --port 80 --default-actions Type=forward,TargetGroupArn=%TG_ARN% --region us-east-1
echo ✅ Listener created

echo 🔄 Updating ECS Service...
aws ecs update-service --cluster seaandtea-cluster --service seaandtea-backend-service --load-balancers targetGroupArn=%TG_ARN%,containerName=seaandtea-backend,containerPort=8080 --region us-east-1
echo ✅ ECS Service updated

echo.
echo 🎉 ALB Setup Complete!
echo 🌐 Your application will be accessible at the ALB DNS name
echo 🏥 Health check endpoint will be available at /actuator/health

