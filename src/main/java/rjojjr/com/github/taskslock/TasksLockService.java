package rjojjr.com.github.taskslock;

import jakarta.annotation.PreDestroy;
import rjojjr.com.github.taskslock.models.TaskLock;

public interface TasksLockService {

    /**
     * Acquire lock for task
     * @param taskName
     * @param contextId
     * @param waitForLock
     * @return
     */
    TaskLock acquireLock(String taskName, String contextId, boolean waitForLock);

    /**
     * Release lock for task
     * @param taskName
     */
    String releaseLock(String taskName);

    /**
     * Acquire lock with embedded impl.
     * @param taskName
     * @param hostName
     * @param contextId
     * @param waitForLock
     * @return
     */
    TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock);

    /**
     * Release all locks & cleanup on shutdown
     */
    @PreDestroy
    void onShutdown();

}
