# Contributing to Lift Bro

First off, thank you for considering contributing to Lift Bro! It's people like you that make Lift Bro a great tool for the fitness community.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [How Can I Contribute?](#how-can-i-contribute)
  - [Reporting Bugs](#reporting-bugs)
  - [Suggesting Enhancements](#suggesting-enhancements)
  - [Pull Requests](#pull-requests)
- [Development Setup](#development-setup)
  - [Prerequisites](#prerequisites)
  - [Building the Project](#building-the-project)
  - [Running Tests](#running-tests)
- [Project Structure](#project-structure)
- [Coding Guidelines](#coding-guidelines)
- [Commit Messages](#commit-messages)

## Code of Conduct

This project and everyone participating in it is governed by a code of respect and professionalism. By participating, you are expected to uphold this standard. Please be kind and considerate in all interactions.

## How Can I Contribute?

### Reporting Bugs

Before creating bug reports, please check the existing issues to avoid duplicates. When you create a bug report, include as many details as possible using our [issue template](.github/ISSUE_TEMPLATE/issue-report.md).

**When filing a bug report, please include:**

- A clear and descriptive title
- Steps to reproduce the issue
- Expected behavior vs. actual behavior
- Screenshots or videos if applicable
- Device information (OS, version, etc.)
- App version

### Suggesting Enhancements

Enhancement suggestions are tracked as GitHub issues. When creating an enhancement suggestion, please include:

- A clear and descriptive title
- A detailed description of the proposed enhancement
- Why this enhancement would be useful to most users
- Possible implementation approaches (if you have ideas)

### Pull Requests

1. **Fork the repository** and create your branch from `main`
2. **Make your changes** following our coding guidelines
3. **Test your changes** thoroughly
4. **Run UI tests** using Maestro (see [Running Tests](#running-tests))
5. **Commit your changes** using clear commit messages
6. **Push to your fork** and submit a pull request

**Pull Request Guidelines:**

- Fill in the PR template completely
- Link any related issues
- Include screenshots for UI changes
- Ensure CI passes (Android/iOS builds and tests)
- Keep PRs focused - one feature/fix per PR when possible
- Be responsive to feedback and questions

## Development Setup

### Prerequisites

**For Android Development:**
- JDK 17 or higher
- Android Studio (latest stable version recommended)
- Android SDK (API 34+)

**For iOS Development:**
- macOS with Xcode 15+
- CocoaPods
- iOS 15+ SDK

**For Both:**
- Kotlin 1.9+
- Gradle 8.0+

### Building the Project

1. **Clone the repository:**
   ```bash
   git clone https://github.com/eholtrop/lift-bro.git
   cd lift-bro
   ```

2. **Set up environment variables** (optional, for full feature access):
   ```bash
   export LIFT_BRO_ADMOB_APP_ID="your_admob_app_id"
   export LIFT_BRO_AD_UNIT_ID="your_ad_unit_id"
   export LIFT_BRO_SENTRY_DSN="your_sentry_dsn"
   export REVENUE_CAT_API_KEY="your_revenue_cat_key"
   ```
   
   Note: These are only needed for AdMob, Sentry, and RevenueCat features. The app will build and run without them.

3. **Build Android:**
   ```bash
   ./gradlew :app-android:assembleDebug
   ```

4. **Build iOS:**
   ```bash
   # From the iosApp directory
   cd iosApp
   pod install
   xcodebuild -workspace iosApp.xcworkspace -scheme iosApp -configuration Debug
   ```

### Running Tests

**UI Tests (Maestro):**

Lift Bro uses [Maestro](https://maestro.mobile.dev/) for UI testing.

1. **Install Maestro:**
   ```bash
   curl -Ls "https://get.maestro.mobile.dev" | bash
   ```

2. **Run all UI tests:**
   ```bash
   maestro test .maestro
   ```

3. **Run specific test flow:**
   ```bash
   maestro test .maestro/onboarding_tests.yaml
   ```

Make sure you have a running emulator/simulator or connected device before running tests.

**Unit Tests:**
```bash
./gradlew test
```

## Project Structure

Lift Bro follows Clean Architecture with Kotlin Multiplatform:

```
lift-bro/
├── app-android/          # Android application module
├── app-ios/              # iOS application module
├── presentation/         # UI layer
│   ├── compose/          # Jetpack Compose UI components
│   └── server/           # API server for JSON models
├── domain/               # Business logic and use cases
├── data/                 # Data layer
│   ├── client/           # Ktor client
│   ├── core/             # Data core
│   └── sqldelight/       # SQLDelight database
└── libs/
    └── mvi/              # Custom MVI implementation
```

### Architecture Notes

- **No ViewModels**: The project uses custom state management with `SavedStateHandle` instead of AndroidX ViewModels
- **No Dagger**: Custom dependency injection (this is experimental and subject to refactoring)
- **MVI Pattern**: State is stored in handlers, side effects handle long-running tasks

For more details, see the [Architecture section in the README](README.md#architecture).

## Coding Guidelines

### Kotlin Style

- Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused on a single responsibility
- Add comments for complex logic, but prefer self-documenting code

### Compose Guidelines

- Keep composables small and reusable
- Hoist state when appropriate
- Use `remember` and `rememberSaveable` correctly
- Follow Material Design 3 guidelines

### Testing

- Write UI tests for critical user flows
- Test edge cases and error scenarios
- Keep tests readable and maintainable

## Commit Messages

Write clear, concise commit messages that explain **what** and **why**, not just **how**.

**Good examples:**
- `Add exercise history graph to track progress over time`
- `Fix crash when deleting workout with no exercises`
- `Refactor database queries to improve performance`

**Bad examples:**
- `Update code`
- `Fix bug`
- `Changes`

## Questions?

If you have questions or need help, feel free to:
- Open an issue with the "question" label
- Reach out via the [Ko-fi page](https://ko-fi.com/B0B71MI0CT)

Thank you for contributing to Lift Bro! 💪
