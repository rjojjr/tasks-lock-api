package rjojjr.com.github.taskslock.exception;

public class TasksLockApiException extends RuntimeException {
    public TasksLockApiException(String message, Exception cause) {
        super(message, cause);
    }
}
