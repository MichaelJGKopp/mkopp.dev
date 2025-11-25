param(
    [string]$Service = "backend"
)

./tools/scripts/api/api_download.ps1 -Service $Service -Url "http://localhost:8200/v3/api-docs"
./tools/scripts/api/api_generate.ps1 -Service $Service
