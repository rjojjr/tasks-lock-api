# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
The Java/Spring Boot module (Embedded Mode, API Mode and API Client Mode) is versioned
with `vX.Y.Z` tags. The Python client in `client-modules/python` is versioned
independently with `Python-Client-vX.Y.Z` tags. Both lines are listed below, newest first.

## [1.2.0] - 2026-09-17

Java module release `1.2.0`. The Python client changes in this release are versioned as
[`Python-Client-v1.0.3`](#python-client-v103---2026-09-17).

### Added
- Optional per-lock timeout. `acquireLock` accepts a `timeoutMinutes` argument via new
  overloads on `TasksLockService`, the API controller and the API client. A lock held
  longer than its timeout is treated as expired and may be acquired by another requester.
- `tasks-lock.default-timeout.minutes` application property to override the default
  timeout of 60 minutes on the module instance that owns the database.
- `timeout_minutes` column on the `task_locks` table in `init.sql`, and a matching field on
  the `TaskLock` and `TaskLockEntity` models and the API response.
- Test suite: `spring-boot-starter-test` and an H2 test database, JUnit platform
  configuration, and timeout tests for `EmbeddedTasksLockService` and `TasksLockApiController`.
- README section documenting lock timeouts for Java consumers.

### Changed
- Docker runtime image switched from `openjdk:17-jdk-slim-bullseye` to
  `eclipse-temurin:17-jre-jammy`.
- Test package renamed from `rjojjr.com.github.taskslocks` to `rjojjr.com.github.taskslock`.

## [Python-Client-v1.0.3] - 2026-09-17

### Added
- `timeout_minutes` keyword argument on `acquire_lock` and a `timeout_minutes` field on the
  `TaskLock` model.
- README example showing a short-lived lock.

### Fixed
- The `SIGTERM` handler is only registered when the client is constructed on the main
  thread. Constructing it from another thread previously raised, since Python only allows
  signal handlers to be set from the main thread.

## [1.1.4] - 2025-09-07

Released as `1.1.4-RELEASE` in `build.gradle`. No `v1.1.3` or `v1.1.4` git tag exists.

### Added
- `releaseLocks` method on `TasksLockService` to release the locks for all tasks, implemented
  in both `EmbeddedTasksLockService` and `TasksLocksApiClientService`.
- `GET /tasks-lock/api/v1/release/all` endpoint that releases all locks.
- `getLocks` method on `TasksLockService` and a `GET /tasks-lock/api/v1` endpoint that lists
  all locks.
- `curl` installed in the Docker image for container health checks.
- `init.sql` with the `task_locks` table definition.

### Changed
- `com.mysql:mysql-connector-j` pinned to `8.2.0`.

## [Python-Client-v1.0.2] - 2025-05-14

### Added
- All held locks are released when the process receives `SIGTERM`. The handler chains
  to any previously registered `SIGTERM` handler.

### Changed
- Package exports refactored so the client and models are easier to import.
- Python client README updated.

## [Python-Client-v1.0.1] - 2024-09-29

### Added
- Logging in the client, including the release-lock method.
- Python client README and the missing dependency file.
- `CONTRIBUTING.md`.

### Fixed
- Environment variable parsing in the Python client.
- Bad URL building in the Python client.

## [Python-Client-v1.0.0] - 2024-09-28

Initial release of the Python client.

### Added
- `TasksLockService` client for the API Mode module with `acquire_lock` and
  `release_lock` methods.
- Separate `models` package with the `TaskLock` model.
- Doc comments on the public client methods.

## [v1.1.2] - 2024-09-14

### Added
- Database-level locking in the `acquireLock` and `releaseLock` methods of
  `EmbeddedTasksLockService`, with documentation of the approach.
- `tasks-lock.retry-interval.ms` application property to control the polling interval
  while waiting for a lock.
- `TODO.md`.

### Changed
- Optimized the Docker build stage.

### Fixed
- `onDestroy` shutdown handling.
- Correctness of the DB-level locking in `EmbeddedTasksLockService`.

## [v1.1.1] - 2024-09-14

### Added
- `tasks-lock.api.enabled=true` set by default in the `tasks-lock-api` application profile.

### Changed
- Refactored the `TasksLockService` interface and standardized API responses.
- Refactored exception handling.
- Extracted `StatefulTasksLockService` and `DestroyableTasksLockService` parent classes to
  remove duplicated `onDestroy` logic between the embedded and API client services.
- Standardized logging across services.
- Documented and optimized the Dockerfile; old builds are removed during the Docker build.
- README updates.

## [v1.1.0] - 2024-09-04

### Added
- Dockerfile for running the module in API Mode.
- Typed exceptions: `AcquireLockFailureException`, `ReleaseLockFailureException`,
  `TasksLockApiException` and `TasksLockShutdownFailure`.
- General exception handling in the API controller.

### Changed
- `acquireLock` returns a `TaskLock` in unlocked status when the lock cannot be
  acquired, instead of returning `null`.
- Race conditions in `EmbeddedTasksLockService` are handled.

### Fixed
- Bad release URL built by the API client.

## [v1.0.0] - 2024-09-04

Initial release.

### Added
- Distributed task locking for Spring Boot applications backed by a MySQL table.
- Embedded Mode (`EmbeddedTasksLockService`), API Mode (`TasksLockApiController`) and
  API Client Mode (`TasksLocksApiClientService`).
- Spring Boot autoconfiguration.
- Gradle build and publish configuration.
- README and MIT license.

### Fixed
- Data constraints on the lock entity.

[1.2.0]: https://github.com/rjojjr/tasks-lock-api/compare/7113c22...v1.2.0
[Python-Client-v1.0.3]: https://github.com/rjojjr/tasks-lock-api/compare/Python-Client-v1.0.2...Python-Client-v1.0.3
[1.1.4]: https://github.com/rjojjr/tasks-lock-api/compare/v1.1.2...7113c22
[Python-Client-v1.0.2]: https://github.com/rjojjr/tasks-lock-api/compare/Python-Client-v1.0.1...Python-Client-v1.0.2
[Python-Client-v1.0.1]: https://github.com/rjojjr/tasks-lock-api/compare/Python-Client-v1.0.0...Python-Client-v1.0.1
[Python-Client-v1.0.0]: https://github.com/rjojjr/tasks-lock-api/releases/tag/Python-Client-v1.0.0
[v1.1.2]: https://github.com/rjojjr/tasks-lock-api/compare/v1.1.1...v1.1.2
[v1.1.1]: https://github.com/rjojjr/tasks-lock-api/compare/v1.1.0...v1.1.1
[v1.1.0]: https://github.com/rjojjr/tasks-lock-api/compare/v1.0.0...v1.1.0
[v1.0.0]: https://github.com/rjojjr/tasks-lock-api/releases/tag/v1.0.0
