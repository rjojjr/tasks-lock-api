package rjojjr.com.github.taskslock.exception;


import lombok.Getter;

@Getter
public class AcquireLockFailureException extends TasksLockApiException {

    private final String taskName;
    private final String contextId;
    public AcquireLockFailureException(String taskName, String contextId, Exception cause) {
        super(String.format("error while acquiring lock for task %s: %s", taskName, cause), cause);
        this.taskName = taskName;
        this.contextId = contextId;
    }
}
