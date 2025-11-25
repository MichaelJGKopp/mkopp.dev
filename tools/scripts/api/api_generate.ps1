<#
.SYNOPSIS
    Generate Angular API client from OpenAPI JSON for Nx workspace.

.PARAMETER Service
    Name of the service (matches folder in api-contracts). Defaults to 'backend'.

.EXAMPLE
    .\generate.ps1 -Service backend
#>

param(
    [string]$Service = "backend"
)

# Paths
$Input = "api-contracts\$Service\openapi.json"
$Output = "libs\api-clients\$Service"

# Convert to forward slashes for OpenAPI generator (required for URI parsing)
$InputPath = $Input -replace '\\', '/'
$OutputPath = $Output -replace '\\', '/'

Write-Host "Generating Angular client for service '$Service'..."
Write-Host "Input: $Input"
Write-Host "Output: $Output"

# Ensure output directory exists
if (-Not (Test-Path $Output)) {
    New-Item -ItemType Directory -Force -Path $Output | Out-Null
}

# Run OpenAPI Generator using local CLI
npx openapi-generator-cli generate `
    -i $InputPath `
    -g typescript-angular `
    -o $OutputPath `
    # --additional-properties=ngVersion=6.1.7,npmName=restClient,supportsES6=true,npmVersion=6.9.0,withInterfaces=true
    # --additional-properties=fileNaming=kebab-case,npmName=ngRestClient
    # ,ngVersion=18,supportsES6=true,npmName=@mkopp/ng-rest-client,supportsES6=true

Write-Host "✅ Angular client generated at $Output"
