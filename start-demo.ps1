$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$backendPath = Join-Path $projectRoot "backend"
$frontendPath = Join-Path $projectRoot "frontend"

Write-Host "Pornesc Employee Leave Hub..." -ForegroundColor Cyan

Start-Process powershell.exe -WorkingDirectory $backendPath -ArgumentList @(
    "-NoExit",
    "-Command",
    ".\mvnw.cmd spring-boot:run"
)

Start-Process powershell.exe -WorkingDirectory $frontendPath -ArgumentList @(
    "-NoExit",
    "-Command",
    "if (-not (Test-Path node_modules)) { npm install }; npm start"
)

Write-Host "Așteaptă aproximativ 20 de secunde, apoi deschide http://localhost:4200" -ForegroundColor Green
Write-Host "Cont rapid: admin@leavehub.ro / Demo123!"
