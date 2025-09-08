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
docker compose -f docker-compose.prod.yml up -d --no-build --remove-orphans --no-deps --wait --force-recreate backend
echo "✅ Backend is healthy and ready."

# Wait for backend to stabilize
echo "⏳ Waiting for backend to stabilize..."
sleep 30

# Update frontend second
echo "⬆️ Updating frontend services..."
docker compose -f docker-compose.prod.yml up -d --no-build --remove-orphans --no-deps --wait --force-recreate frontend
echo "✅ Frontend is healthy and ready."

echo "🧹 Cleaning up old images..."
docker image prune -f

echo "✅ Deployment complete!"
