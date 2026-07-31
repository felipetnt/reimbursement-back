$ErrorActionPreference = "Stop"

$openssl = Get-Command openssl -ErrorAction SilentlyContinue

if ($null -eq $openssl) {
    throw "OpenSSL não foi encontrado no PATH. Abra o Git Bash e execute os comandos do README, ou adicione o OpenSSL ao PATH."
}

$resources = Join-Path $PSScriptRoot "src\main\resources"
New-Item -ItemType Directory -Path $resources -Force | Out-Null

$rsaPrivate = Join-Path $resources "rsaPrivateKey.pem"
$privateKey = Join-Path $resources "privateKey.pem"
$publicKey = Join-Path $resources "publicKey.pem"

& openssl genrsa -out $rsaPrivate 2048
& openssl rsa -pubout -in $rsaPrivate -out $publicKey
& openssl pkcs8 -topk8 -nocrypt -inform pem `
    -in $rsaPrivate -outform pem -out $privateKey

Remove-Item $rsaPrivate -Force

Write-Host ""
Write-Host "Chaves JWT criadas em:"
Write-Host $privateKey
Write-Host $publicKey
Write-Host ""
Write-Host "Não envie privateKey.pem para o GitHub."
