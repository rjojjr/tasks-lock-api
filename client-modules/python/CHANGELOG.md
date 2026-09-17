# Changelog - TasksLock API Python Client

All notable changes to the Python client in `client-modules/python` are documented here.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
The client is versioned independently of the Java module with `Python-Client-vX.Y.Z`
git tags, and the version string lives in `src/taskslock/client/tasks_lock_service.py`.
The repository-wide changelog is at [`../../CHANGELOG.md`](../../CHANGELOG.md).

## [1.0.3] - 2026-09-17

### Added
- `timeout_minutes` keyword argument on `TasksLockService.acquire_lock`. When set, it is
  sent to the API as `timeoutMinutes` and the lock is treated as expired once it has been
  held that long. When omitted, the API's configured default of 60 minutes applies.
- `timeout_minutes` attribute on the `TaskLock` model, populated from the API response.
- README example showing a short-lived lock.

### Fixed
- The `SIGTERM` handler is only registered when `TasksLockService` is constructed on the
  main thread. Python only allows signal handlers to be set from the main thread, so
  constructing the client from a worker thread previously raised a `ValueError`. Off the
  main thread the client now logs a debug message and skips the handler.

## [1.0.2] - 2025-05-14

### Added
- All held locks are released when the process receives `SIGTERM`. The client tracks
  every acquired lock and the signal handler calls `release_all_locks` before chaining to
  any previously registered `SIGTERM` handler.
- `TasksLockService.release_all_locks` public method.

### Changed
- `TasksLockService` and `TaskLock` are exported from the `taskslock.client` package, so
  `from taskslock.client import TasksLockService` works directly.
- README updated for the new import path.

## [1.0.1] - 2024-09-29

### Added
- Debug and warning logging on lock acquisition and release, using the standard `logging` module.
- `requirements.in` pinned dependency file, which had been missing.
- Python client README.
- Repository `CONTRIBUTING.md`.

### Fixed
- Parsing of the `TASKS_LOCK_API_HOST`, `TASKS_LOCK_API_PROTOCOL` and `TASKS_LOCK_API_PORT`
  environment variables.
- URL building for the acquire and release API calls.

## [1.0.0] - 2024-09-28

Initial release of the Python client.

### Added
- `TasksLockService` client for a TasksLock API Mode instance with `acquire_lock` and
  `release_lock` methods. The API location is taken from constructor arguments or the
  `TASKS_LOCK_API_PROTOCOL`, `TASKS_LOCK_API_HOST` and `TASKS_LOCK_API_PORT` environment
  variables, defaulting to `http://localhost:8080`.
- `TaskLock` model in a separate `models` package, exposing `task_name`, `context_id`,
  `is_locked` and a `release` callable.
- `version` attribute on the service instance.
- Doc comments on the public client methods.

### Changed
- `acquire_lock` returns an unlocked `TaskLock` instead of `None` when the lock cannot be acquired.
- Renamed the lock-status property on the model to `is_locked`.

[1.0.3]: https://github.com/rjojjr/tasks-lock-api/compare/Python-Client-v1.0.2...Python-Client-v1.0.3
[1.0.2]: https://github.com/rjojjr/tasks-lock-api/compare/Python-Client-v1.0.1...Python-Client-v1.0.2
[1.0.1]: https://github.com/rjojjr/tasks-lock-api/compare/Python-Client-v1.0.0...Python-Client-v1.0.1
[1.0.0]: https://github.com/rjojjr/tasks-lock-api/releases/tag/Python-Client-v1.0.0
