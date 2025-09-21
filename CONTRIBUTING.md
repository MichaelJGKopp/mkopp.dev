# Contributing to mkopp.dev

First off, thank you for considering contributing to mkopp.dev! It’s people like you that make open source such a great community.

## Where do I start?

### Reporting Bugs

If you find a bug, please open an issue on our [GitHub Issues page](https://github.com/MichaelJGKopp/mkopp.dev/issues). Please include as much detail as possible, including:

- A clear and descriptive title.
- A detailed description of the bug, including steps to reproduce it.
- The expected behavior and what actually happened.
- Your environment details (OS, browser, etc.).

### Suggesting Enhancements

If you have an idea for a new feature or an improvement to an existing one, please open an issue on our [GitHub Issues page](https://github.com/MichaelJGKopp/mkopp.dev/issues). Please provide a clear and detailed explanation of the feature, why it would be beneficial, and any potential implementation ideas.

## Development Setup

Please refer to the [Local Development](./README.md#local-development) section in the main `README.md` file for instructions on how to set up your development environment.

## Pull Request Process

1. Ensure any install or build dependencies are removed before the end of the layer when doing a build.
2. Update the `README.md` with details of changes to the interface, this includes new environment variables, exposed ports, useful file locations and container parameters.
3. Increase the version numbers in any examples and the `README.md` to the new version that this Pull Request would represent. The versioning scheme we use is [SemVer](http://semver.org/).
4. You may merge the Pull Request in once you have the sign-off of two other developers, or if you do not have permission to do that, you may request the second reviewer to merge it for you.

## Coding Style

Please follow the coding style of the existing codebase. We use Prettier and ESLint to enforce a consistent style. You can run the following command to check your code:

```sh
npx nx run-many --target=lint
```

## Code of Conduct

This project and everyone participating in it is governed by the [mkopp.dev Code of Conduct](./CODE_OF_CONDUCT.md). By participating, you are expected to uphold this code. Please report unacceptable behavior to <contact@mkopp.dev> .
