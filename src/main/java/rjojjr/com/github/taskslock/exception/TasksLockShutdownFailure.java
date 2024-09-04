package rjojjr.com.github.taskslock.exception;

public class TasksLockShutdownFailure extends TasksLockApiException {
    public TasksLockShutdownFailure(Exception cause) {
        super("failed to run TasksLock API shutdown procedure", cause);
    }
}
