# CI/CD Pipeline

This document provides an overview of the CI/CD pipeline for the mkopp.dev project.

## Overview

The CI/CD pipeline is implemented using GitHub Actions. The pipeline is triggered on every push to the `main` branch and is responsible for building, testing, and deploying the application to the production environment.

## Pipeline Stages

The pipeline consists of the following stages:

1.  **Build & Test:** The frontend and backend applications are built and tested.
2.  **Publish Docker Images:** On success, multi-stage Docker images are built and pushed to DockerHub.
3.  **Deploy to VPS:** The `deploy.sh` script is executed on the production VPS via SSH. This script pulls the latest images from DockerHub and restarts the services using `docker-compose -f docker-compose.prod.yml up -d`.

## Visualization

A visualization of the pipeline can be found in the [pipeline.mmd](./pipeline.mmd) file.

## Further Reading

For more details on the CI/CD pipeline, please refer to [ADR 0004 – Deployment with Docker + Traefik](../adr/0004-deployment-with-docker-and-traefik.md).
