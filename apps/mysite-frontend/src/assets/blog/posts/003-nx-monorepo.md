# Taming the Monorepo: How We Use Nx to Manage Our Full-Stack Application

This article explains why we chose a monorepo approach for this project and how we use Nx to manage our full-stack application, which consists of an Angular frontend and a Spring Boot backend.

## What is a Monorepo? And Why Use One?

A monorepo is a single repository that contains the code for multiple projects. For a full-stack application, this means having the frontend and backend code in the same repository.

This approach has several benefits:

*   **Atomic Commits**: Changes to both the frontend and backend can be made in a single commit, making it easier to track and understand changes across the entire application.
*   **Code Sharing**: It's easy to share code between the frontend and backend, or between different applications in the monorepo.
*   **Single Source of Truth**: There is one place for all the code, which simplifies dependency management and builds.

## Why Nx?

While a monorepo offers many benefits, it can also become complex to manage as the project grows. This is where Nx comes in.

Nx is a smart, extensible build framework that helps you manage monorepos. As detailed in our [ADR on using an Nx Monorepo](./../docs/adr/0002-nx-monorepo.md), we chose Nx for several reasons:

*   **Excellent Tooling**: Nx provides a rich set of tools for managing monorepos, including generators for creating new applications and libraries, and executors for running tasks like building, testing, and linting.
*   **Dependency Graph**: Nx understands the dependencies between projects in the monorepo, which allows it to do smart things like only rebuilding and re-testing the parts of the application that have changed.
*   **Caching**: Nx can cache the results of builds and tests, which can significantly speed up development.
*   **Plugins**: Nx has a rich ecosystem of plugins for different technologies. In our project, we use plugins for Angular, Spring Boot, Jest, ESLint, and Playwright.

## Our Project Structure

Our Nx workspace is organized into two main folders:

*   `apps`: This folder contains our two main applications:
    *   `mysite-frontend`: The Angular SSR frontend.
    *   `mysite-backend`: The Spring Boot backend.
*   `libs`: This folder is currently empty, but it's where we would put any shared code or libraries that might be used by both the frontend and backend in the future.

This structure provides a clear separation of concerns and makes it easy to navigate the codebase.

## Nx in Action: A Look at Our Configuration

Our Nx configuration is defined in the `nx.json` file. This file configures target defaults, plugins, and generators.

### Key Plugins

We use several Nx plugins to manage our full-stack application:

*   `@nx/angular`: Provides generators and executors for Angular development.
*   `@nxrocks/nx-spring-boot`: Integrates our Spring Boot backend into the Nx workspace, allowing us to build, test, and run it using Nx commands.
*   `@nx/jest`: For running unit tests.
*   `@nx/eslint`: For linting our code.
*   `@nx/playwright`: For end-to-end testing.

These plugins provide a consistent way to manage our projects and ensure that we follow best practices.

## Conclusion

Using an Nx monorepo has been a great choice for this project. It provides a solid foundation for our full-stack application and has allowed us to maintain a clean, organized, and scalable codebase. While some of the more advanced features of Nx might seem like overkill for a project of this size, they provide a clear path for future growth and ensure that we are following industry best practices from the start.
