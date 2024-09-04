package rjojjr.com.github.taskslock.exception;

public class ReleaseLockFailureException extends TasksLockApiException {
    public ReleaseLockFailureException(String taskName, Exception cause) {
        super(String.format("error while releasing lock for %s: %s", taskName, cause.getMessage()), cause);
    }
}
