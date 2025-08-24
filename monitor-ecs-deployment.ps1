# PowerShell script to monitor ECS deployment status

param(
    [string]$ClusterName = "seaandtea-cluster",
    [string]$ServiceName = "seaandtea-backend-service",
    [string]$Region = "us-east-1"
)

Write-Host "🔍 Monitoring ECS deployment..." -ForegroundColor Green
Write-Host "📋 Cluster: $ClusterName" -ForegroundColor Cyan
Write-Host "📋 Service: $ServiceName" -ForegroundColor Cyan
Write-Host "🌍 Region: $Region" -ForegroundColor Cyan
Write-Host ""

while ($true) {
    try {
        # Get service status
        $service = aws ecs describe-services --cluster $ClusterName --services $ServiceName --region $Region --output json | ConvertFrom-Json
        
        if ($service.services) {
            $svc = $service.services[0]
            
            Write-Host "⏰ $(Get-Date -Format 'HH:mm:ss') - Service Status: $($svc.status)" -ForegroundColor Yellow
            Write-Host "   📊 Desired: $($svc.desiredCount) | Running: $($svc.runningCount) | Pending: $($svc.pendingCount)" -ForegroundColor White
            
            # Check deployments
            foreach ($deployment in $svc.deployments) {
                Write-Host "   🚀 Deployment: $($deployment.status) - $($deployment.rolloutState)" -ForegroundColor Cyan
                Write-Host "      📈 Desired: $($deployment.desiredCount) | Running: $($deployment.runningCount) | Pending: $($deployment.pendingCount)" -ForegroundColor White
            }
            
            # Check recent events
            if ($svc.events -and $svc.events.Count -gt 0) {
                Write-Host "   📝 Recent Events:" -ForegroundColor Magenta
                foreach ($event in $svc.events[0..2]) {
                    Write-Host "      • $($event.message)" -ForegroundColor Gray
                }
            }
            
            # Check if service is stable
            if ($svc.runningCount -eq $svc.desiredCount -and $svc.pendingCount -eq 0) {
                Write-Host "✅ Service is stable and running!" -ForegroundColor Green
                break
            }
        }
        
        Write-Host ""
        Start-Sleep -Seconds 10
        
    } catch {
        Write-Host "❌ Error monitoring service: $_" -ForegroundColor Red
        break
    }
}

Write-Host ""
Write-Host "🎯 Final Status Check:" -ForegroundColor Green
aws ecs describe-services --cluster $ClusterName --services $ServiceName --region $Region --query 'services[0].{Status:status,Desired:desiredCount,Running:runningCount,Pending:pendingCount}' --output table

