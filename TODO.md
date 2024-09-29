# TasksLock API TODO

This document contains a list of things that
would make this project better.

## TODOS

Branch names and commit messages should start with `TODO-{todo number in list}`.

1. Create class objects for method arguments so that they are easier to change
2. Add annotation for locking methods instead of methods
3. Add client-only build variant that doesn't contain Spring Data dependencies
4. Add timeout argument when waiting for a lock
5. Add migration runner for API and embedded mode
6. Add client libraries for other languages:
   1. ~~python~~
      1. Add release all locks on shutdown
      2. Host python client on pypi
   2. go
   3. javascript
      1. Host on npm repository
   4. rust
   5. dotnet
7. Add ability to hold/configure multiple locks for a task
8. Add authentication for TasksLockAPI
   1. Basic auth token
   2. JWT 