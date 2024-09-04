package rjojjr.com.github.taskslock.exception;

public class AcquireLockFailureException extends TasksLockApiException {
    public AcquireLockFailureException(String taskName, Exception cause) {
        super(String.format("error while acquiring lock for task %s: %s", taskName, cause), cause);
    }
}
