#!/bin/bash
set -euo pipefail

IMAGE_NAME="tcmenu-web-generator"
IMAGE_TAG="latest"
CONTAINER_NAME="tcmenu-web-service"
PORT="8080"

echo "=== 1. Building React Frontend (tcmenugen) ==="
cd web-designer/tcmenugen
npm install
npm run build
cd ../..

echo "=== 2. Building Maven Backend (tcmenu-web-generator) ==="
mvn clean package -pl web-designer/tcmenu-web-generator -am -DskipTests

echo "=== 3. Building Docker Image (${IMAGE_NAME}:${IMAGE_TAG}) ==="
docker build -t "${IMAGE_NAME}:${IMAGE_TAG}" -f Dockerfile .

echo "=== 4. Running Docker Container ==="
# Stop and remove existing container if running
if [ "$(docker ps -aq -f name=^/${CONTAINER_NAME}$)" ]; then
    echo "Stopping and removing existing container ${CONTAINER_NAME}..."
    docker stop "${CONTAINER_NAME}" || true
    docker rm "${CONTAINER_NAME}" || true
fi

docker run -d \
    --name "${CONTAINER_NAME}" \
    -p "${PORT}:8080" \
    --restart unless-stopped \
    "${IMAGE_NAME}:${IMAGE_TAG}"

echo "=== Container started successfully on http://localhost:${PORT} ==="
