# Contributing

Thank you for considering contributing to this project! We welcome contributions from everyone.

## How to Contribute

### Reporting Bugs

If you find a bug, please open an issue with:
- A clear, descriptive title
- Steps to reproduce the issue
- Expected behavior vs actual behavior
- Your environment (OS, Kotlin version, etc.)
- Code samples or screenshots if applicable

### Suggesting Features

Feature requests are welcome! Please open an issue with:
- A clear description of the feature
- Use cases and benefits
- Any relevant examples or mockups

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following the code style guidelines below
3. **Add tests** if applicable
4. **Update documentation** (README, KDoc, etc.) as needed
5. **Ensure tests pass**: Run `./gradlew test`
6. **Ensure linting passes**: Run `./gradlew detekt` (if available)
7. **Commit your changes** with clear, descriptive commit messages
8. **Push to your fork** and submit a pull request

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions focused and concise
- Write tests for new functionality

### Building Locally

```bash
# Clone the repository
git clone <repository-url>
cd <project-directory>

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run linting (if available)
./gradlew detekt
```

### Kotlin Multiplatform Considerations

This is a Kotlin Multiplatform project supporting:
- Android (minSdk 24, compileSdk 36)
- iOS (iosX64, iosArm64, iosSimulatorArm64)

When adding platform-specific code:
- Use `expect`/`actual` declarations
- Place common code in `commonMain`
- Place platform-specific implementations in `androidMain`, `iosMain`, or `nativeMain`
- Test on multiple platforms when possible

### Commit Messages

- Use the present tense ("Add feature" not "Added feature")
- Use the imperative mood ("Move cursor to..." not "Moves cursor to...")
- Limit the first line to 72 characters
- Reference issues and pull requests when relevant

### Code of Conduct

- Be respectful and inclusive
- Welcome newcomers and help them get started
- Accept constructive criticism gracefully
- Focus on what's best for the community

## Questions?

Feel free to open an issue for questions or discussions about contributing.

## License

By contributing, you agree that your contributions will be licensed under the Apache License 2.0.
