# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.1.0] - TBD

### Added
- Initial release of MVI (Model-View-Intent) library for Kotlin Multiplatform
- Reducer: pure (State, Event) -> State function
- SideEffect: suspend (State, Event) -> Unit for async operations
- Interactor: orchestrates Flow<State> + Event stream into StateFlow<State>
- Compose integration with rememberInteractor()
- Automatic state persistence via rememberSaveable
- Support for upstream data sources via Flow<State>
- Multi-module structure (core, compose)
