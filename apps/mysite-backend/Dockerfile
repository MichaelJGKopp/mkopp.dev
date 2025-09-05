# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

# The source path is relative to the build context (the workspace root)
COPY apps/mysite-backend/pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the application source code
COPY apps/mysite-backend/src ./src

# Package the application
RUN mvn package -DskipTests

# Stage 2: Create the final image
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create a non-root user
RUN groupadd --gid 1001 appuser && \
    useradd --uid 1001 --gid 1001 -m appuser

# Copy the JAR file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Copy the entrypoint script
COPY apps/mysite-backend/entrypoint.sh . 
RUN chmod +x entrypoint.sh

# Switch to the non-root user
USER appuser

# Expose the application port
EXPOSE 8200

# Run the application
ENTRYPOINT ["./entrypoint.sh"]