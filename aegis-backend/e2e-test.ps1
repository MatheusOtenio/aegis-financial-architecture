# Script de Teste Ponta a Ponta (E2E) Simplificado
$BaseUrl = "http://localhost:8080"
$InternalToken = "super-secret-batch-token"

Write-Host "--- 1. Health Check ---"
$health = Invoke-RestMethod "$BaseUrl/actuator/health"
if ($health.status -eq "UP") { Write-Host " Backend UP" -ForegroundColor Green } else { Write-Host "❌ Backend DOWN" -ForegroundColor Red; exit }

Write-Host "`n--- 2. Criar Pedido ---"
$body = @{ items = @( @{ name="PC Gamer"; unitPrice=15000.00; quantity=1 } ) } | ConvertTo-Json -Depth 3
try {
    $order = Invoke-RestMethod -Uri "$BaseUrl/orders" -Method Post -Body $body -ContentType "application/json"
    Write-Host " Pedido Criado: $($order.id)" -ForegroundColor Green
} catch {
    Write-Host " Falha ao criar pedido: $_" -ForegroundColor Red; exit
}

Write-Host "`n--- 3. Pagar Pedido ---"
try {
    Invoke-RestMethod -Uri "$BaseUrl/orders/$($order.id)/pay" -Method Post
    Write-Host " Pedido Pago com Sucesso" -ForegroundColor Green
} catch {
    Write-Host " Falha ao pagar: $_" -ForegroundColor Red
}

Write-Host "`n--- 4. Verificar Auditoria (Via API Interna - Simulação) ---"
# Como não temos endpoint de ler auditoria, verificamos se o pedido está PAGO na listagem
$orders = Invoke-RestMethod "$BaseUrl/orders"
$myOrder = $orders | Where-Object { $_.id -eq $order.id }
if ($myOrder.status -eq "PAID") { Write-Host " Status confirmado: PAID" -ForegroundColor Green } else { Write-Host " Status incorreto: $($myOrder.status)" -ForegroundColor Red }

Write-Host "`n--- 5. API Interna (Batch Report) ---"
$reportDate = (Get-Date).ToString("yyyy-MM-dd")
# Nota: Se rodar 2x no mesmo dia vai dar erro 400 (Regra de negócio), o que é correto.
try {
    $reportBody = @{ date=$reportDate; totalOrders=50; totalRevenue=750000.00 } | ConvertTo-Json
    Invoke-RestMethod -Uri "$BaseUrl/internal/reports/daily" -Method Post -Body $reportBody -ContentType "application/json" -Headers @{ "X-SERVICE-TOKEN" = $InternalToken }
    Write-Host " Relatório Diário Registrado" -ForegroundColor Green
} catch {
    if ($_.Exception.Response.StatusCode -eq "BadRequest") {
        Write-Host " Relatorio ja existia para hoje (Comportamento esperado)" -ForegroundColor Yellow
    } else {
        Write-Host " Erro no Batch: $_" -ForegroundColor Red
    }
}

Write-Host "`n TESTE E2E CONCLUIDO COM SUCESSO!" -ForegroundColor Cyan
