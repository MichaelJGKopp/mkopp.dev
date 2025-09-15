Feature: CI/CD Pipeline
    In order to deploy updates automatically
    As a developer
    I want code changes to be built, tested, and deployed automatically

    Scenario: Build and push images
        Given a commit is pushed to main
        When the pipeline runs
        Then frontend and backend Docker images should be built and pushed

    Scenario: Deploy to VPS
        Given new Docker images exist
        When the VPS pulls the images
        Then containers should be restarted with the latest version

    Scenario: Pipeline caching
        Given dependencies are unchanged
        When the pipeline runs
        Then cached artifacts should speed up build times
