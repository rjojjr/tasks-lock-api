# TasksLock API Python Client README

The Python TasksLock API client allows any Python application to consume locks
from a TasksLock API instance.

## Using This Client

### Install Dependencies

To install dependencies:

```shell
pip install -r requirements.in
```

### Client Usage

To use the client:

```python
# Import the client
from taskslock.client import TasksLockService

# Construct a TasksLockService instance
tasks_lock_service = TasksLockService()


def some_sync_func_that_does_not_wait():
    task_lock = tasks_lock_service.acquire_lock('some-task-name', 'some-context-id', False)
    if task_lock.is_locked:
        # Lock acquired, do something and release lock
        ...
        task_lock.release()
    else:
        # Lock not acquired, handle that case
        ...


def some_sync_func_that_waits():
    # Blocks until lock is acquired
    task_lock = tasks_lock_service.acquire_lock('some-task-name', 'some-context-id', True)
    # No need to check if lock acquired because method blocks until a lock is acquired
    # Do something and release lock
    ...
    task_lock.release()


def some_sync_func_with_a_short_lived_lock():
    # The optional timeout_minutes argument is the lock timeout in minutes(the API default of 60 is used when omitted).
    # If this lock is still held after 5 minutes, it is considered expired and another requester may acquire it.
    task_lock = tasks_lock_service.acquire_lock('some-task-name', 'some-context-id', True, timeout_minutes=5)
    # Do something and release lock
    ...
    task_lock.release()

```