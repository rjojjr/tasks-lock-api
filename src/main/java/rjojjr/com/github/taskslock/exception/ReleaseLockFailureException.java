package rjojjr.com.github.taskslock.exception;

import lombok.Getter;

public class ReleaseLockFailureException extends TasksLockApiException {
    @Getter
    private final String taskName;

    @Getter
    private final String contextId;
    public ReleaseLockFailureException(String taskName, String contextId, Exception cause) {
        super(String.format("error while releasing lock for %s: %s", taskName, cause.getMessage()), cause);
        this.taskName = taskName;
        this.contextId = contextId;
    }
}
