<#
.SYNOPSIS
    Download OpenAPI JSON from a running Spring Boot backend.
.PARAMETER Service
    Name of the service (matches folder in api-contracts). Defaults to 'backend'.
.PARAMETER Url
    URL of the OpenAPI JSON endpoint. Defaults to http://api.localhost/v3/api-docs
#>

param(
    [string]$Service = "backend",
    [string]$Url = "http://api.localhost/v3/api-docs"
)

$OutputFolder = "api-contracts\$Service"
$OutputFile = Join-Path $OutputFolder "openapi.json"

# Ensure folder exists
if (-Not (Test-Path $OutputFolder)) {
    New-Item -ItemType Directory -Force -Path $OutputFolder | Out-Null
}

Write-Host "Downloading OpenAPI spec for service '$Service' from $Url..."

try {
    Invoke-WebRequest $Url -o $OutputFile
    Write-Host "✅ Saved OpenAPI JSON to $OutputFile"
} catch {
    Write-Error "Failed to download OpenAPI spec. Make sure the backend is running."
    exit 1
}
