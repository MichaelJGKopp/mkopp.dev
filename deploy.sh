#!/bin/bash
set -e

# This script is called by the GitHub Actions workflow.
# It expects FRONTEND_TAG and BACKEND_TAG environment variables to be set.

# cd "$DEPLOY_PATH"

echo "🚀 Starting deployment..."

# Load environment variables from .env file for secrets and other configurations
# if [ -f .env ]; then
#   export $(cat .env | sed 's/#.*//g' | xargs)
# fi

# Check if tags are set
if [ -z "$FRONTEND_TAG" ] || [ -z "$BACKEND_TAG" ]; then
  echo "❌ Error: FRONTEND_TAG and BACKEND_TAG must be set." >&2
  exit 1
fi

echo "Deploying Frontend Tag: $FRONTEND_TAG"
echo "Deploying Backend Tag: $BACKEND_TAG"

# Log in to Docker Hub to ensure we can pull images
# DOCKERHUB_USERNAME and DOCKERHUB_TOKEN should be in the .env file
# echo "🔑 Logging in to Docker Hub..."
# echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

# Update backend first
echo "⬆️ Updating backend services..."
docker compose -f docker-compose.prod.yml up -d --no-build --remove-orphans backend

# After backend update
echo "🔍 Checking backend health..."
if docker compose -f docker-compose.prod.yml exec backend curl -sf http://localhost:8200/management/health > /dev/null; then
  echo "✅ Backend is healthy"
else
  echo "❌ Backend health check failed"
  exit 1
fi

# Wait for backend to stabilize
echo "⏳ Waiting for backend to stabilize..."
sleep 30

# Update frontend second
echo "⬆️ Updating frontend services..."
docker compose -f docker-compose.prod.yml up -d --no-build --remove-orphans frontend

# After frontend update
echo "🔍 Checking frontend health..."
if curl -sf http://localhost/healthz > /dev/null; then
  echo "✅ Frontend is healthy"
else
  echo "❌ Frontend health check failed"
  exit 1
fi

# echo "⬆️ Starting services with new images..."
# docker compose up -d -f docker-compose.prod.yml --no-build --remove-orphans

echo "🧹 Cleaning up old images..."
docker image prune -f

echo "✅ Deployment complete!"
