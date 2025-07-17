fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android build_release

```sh
[bundle exec] fastlane android build_release
```

Build the release artifact

### android run_android_emulator

```sh
[bundle exec] fastlane android run_android_emulator
```

Install and run Emulator

### android test

```sh
[bundle exec] fastlane android test
```

Run the project tests

### android lint

```sh
[bundle exec] fastlane android lint
```

Run the project Linter

### android deploy

```sh
[bundle exec] fastlane android deploy
```

Deploy a new version to the Google Play

### android promote_internal_to_prod

```sh
[bundle exec] fastlane android promote_internal_to_prod
```

Promote internal build to prod on Google Play

----


## iOS

### ios match_development

```sh
[bundle exec] fastlane ios match_development
```

Sync iOS Development Code Signing Identities using match

### ios match_appstore

```sh
[bundle exec] fastlane ios match_appstore
```

Sync iOS App Store Code Signing Identities using match

### ios build_release

```sh
[bundle exec] fastlane ios build_release
```

Build the release artifact for iOS

### ios test

```sh
[bundle exec] fastlane ios test
```

Run tests for iOS

### ios lint

```sh
[bundle exec] fastlane ios lint
```

Run Linter for iOS (e.g., SwiftLint)

### ios beta

```sh
[bundle exec] fastlane ios beta
```

Deploy a new beta version to TestFlight for iOS

### ios deploy

```sh
[bundle exec] fastlane ios deploy
```

Submit a new version to the App Store for iOS

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
