$ErrorActionPreference = "Stop"

$IMAGE_NAME = "tcmenu-web-generator"
$IMAGE_TAG = "latest"
$CONTAINER_NAME = "tcmenu-web-service"
$PORT = "8080"

Write-Host "=== 1. Building React Frontend (tcmenugen) ==="
Push-Location web-designer/tcmenugen
try {
    npm install
    if ($LASTEXITCODE -ne 0) { throw "npm install failed with exit code $LASTEXITCODE" }
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "npm run build failed with exit code $LASTEXITCODE" }
}
finally {
    Pop-Location
}

Write-Host "=== 2. Building Maven Backend (tcmenu-web-generator) ==="
mvn clean package -pl web-designer/tcmenu-web-generator -am -DskipTests
if ($LASTEXITCODE -ne 0) { throw "Maven build failed with exit code $LASTEXITCODE" }

Write-Host "=== 3. Building Docker Image ($IMAGE_NAME:$IMAGE_TAG) ==="
docker build -t "$IMAGE_NAME:$IMAGE_TAG" -f Dockerfile .
if ($LASTEXITCODE -ne 0) { throw "Docker build failed with exit code $LASTEXITCODE" }

Write-Host "=== 4. Running Docker Container ==="
$existing = docker ps -aq -f "name=^/${CONTAINER_NAME}$"
if ($existing) {
    Write-Host "Stopping and removing existing container $CONTAINER_NAME..."
    docker stop $CONTAINER_NAME 2>$null
    docker rm $CONTAINER_NAME 2>$null
}

docker run -d `
    --name $CONTAINER_NAME `
    -p "${PORT}:8080" `
    --restart unless-stopped `
    "$IMAGE_NAME:$IMAGE_TAG"
if ($LASTEXITCODE -ne 0) { throw "Docker run failed with exit code $LASTEXITCODE" }

Write-Host "=== Container started successfully on http://localhost:$PORT ==="
