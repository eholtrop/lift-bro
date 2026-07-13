fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

### generate_backup_data

```sh
[bundle exec] fastlane generate_backup_data
```

Generate fresh backup data for screenshots

----


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

### android push_backup_to_device

```sh
[bundle exec] fastlane android push_backup_to_device
```

Push test backup file to Android device/emulator

### android generate_backup_data

```sh
[bundle exec] fastlane android generate_backup_data
```

Generate fresh backup data for screenshots

### android generate_play_store_screenshots

```sh
[bundle exec] fastlane android generate_play_store_screenshots
```

Generate Play Store screenshots locally

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

### ios build_debug

```sh
[bundle exec] fastlane ios build_debug
```

Build the debug artifact for iOS

### ios build_debug_device

```sh
[bundle exec] fastlane ios build_debug_device
```

Build debug artifact for physical iOS device

### ios build_release_device

```sh
[bundle exec] fastlane ios build_release_device
```

Build release artifact for physical iOS device (development signing)

### ios test

```sh
[bundle exec] fastlane ios test
```



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

### ios push_backup_to_simulator

```sh
[bundle exec] fastlane ios push_backup_to_simulator
```

Push test backup file to iOS simulator

### ios generate_app_store_screenshots

```sh
[bundle exec] fastlane ios generate_app_store_screenshots
```

Generate App Store screenshots locally

### ios deploy

```sh
[bundle exec] fastlane ios deploy
```

Submit a new version to the App Store for iOS

### ios latest_testflight_version

```sh
[bundle exec] fastlane ios latest_testflight_version
```

Get the latest TestFlight build version

### ios promote

```sh
[bundle exec] fastlane ios promote
```

Submit existing TestFlight build for App Store review

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
