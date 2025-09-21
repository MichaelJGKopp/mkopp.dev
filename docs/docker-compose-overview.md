# Docker Compose Service Key Order Overview

This document outlines a recommended logical order for defining keys within a Docker Compose service, grouped by their primary function. This structure aids readability and maintainability of `docker-compose.yml` files.

## 1. Identity & Source: What is the Service?
Defines the core image or build process and the container's identity.

*   `image` or `build`
*   `container_name`

## 2. Runtime & Execution: How is the Container Configured and Started?
Specifies commands, entrypoints, and environment settings for the container's operation.

*   `command`
*   `entrypoint`
*   `environment`
*   `secrets`

## 3. Network & Connections: How Does the Service Communicate?
Details how the service interacts with other services and the host network.

*   `ports`
*   `networks`
*   `depends_on`

## 4. Storage & Data: Where is Data Stored?
Defines how persistent data is managed for the service.

*   `volumes`

## 5. Metadata & Orchestration: Additional Configuration for Other Tools.
Includes labels and other settings primarily used by orchestrators or external tools like Traefik.

*   `labels`

## 6. Lifecycle & State: How is the Container Managed?
Covers health checks, restart policies, and deployment-specific configurations.

*   `healthcheck`
*   `restart`
*   `deploy`
