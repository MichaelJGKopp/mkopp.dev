#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# This script is for deploying the application to a Docker Swarm cluster.
# It expects DOCKER_USERNAME, FRONTEND_TAG, and BACKEND_TAG to be set.

STACK_NAME=mysite

# --- Pre-flight Checks ---
if [ -z "$DOCKER_USERNAME" ] || [ -z "$FRONTEND_TAG" ] || [ -z "$BACKEND_TAG" ]; then
  echo "❌ Error: DOCKER_USERNAME, FRONTEND_TAG, and BACKEND_TAG must be set." >&2
  exit 1
fi

# --- Pull Stage ---
echo "Pulling production images from Docker Hub..."

FRONTEND_IMAGE="${DOCKER_USERNAME}/mysite-frontend:${FRONTEND_TAG}"
BACKEND_IMAGE="${DOCKER_USERNAME}/mysite-backend:${BACKEND_TAG}"

docker pull $FRONTEND_IMAGE
docker pull $BACKEND_IMAGE

echo "Pulls complete."

# --- Deploy Stage ---
# We export the image names so they can be substituted in the compose file.
echo "Deploying stack ${STACK_NAME} to Docker Swarm..."

export FRONTEND_IMAGE
export BACKEND_IMAGE

docker stack deploy -c docker/docker-compose.swarm.yml ${STACK_NAME}

echo "Deployment initiated."
echo "Run 'docker stack services ${STACK_NAME}' to check the status."