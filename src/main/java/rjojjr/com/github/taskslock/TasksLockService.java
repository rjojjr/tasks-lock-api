package rjojjr.com.github.taskslock;

import jakarta.annotation.PreDestroy;
import rjojjr.com.github.taskslock.models.TaskLock;

public interface TasksLockService {

    /**
     * Acquire lock for task
     * @param taskName unique task identifier
     * @param contextId a tracing identifier provided by the consumer
     * @param waitForLock block until lock is acquired
     * @return TaskLock object
     */
    TaskLock acquireLock(String taskName, String contextId, boolean waitForLock);

    /**
     * Release lock for task
     * @param taskName unique task identifier
     * @return contextId a tracing identifier provided by the consumer
     */
    String releaseLock(String taskName);

    /**
     * Acquire lock with embedded impl.
     * @param taskName unique task identifier
     * @param hostName hostname of application/container acquiring lock
     * @param contextId a tracing identifier provided by the consumer
     * @param waitForLock block until lock is acquired
     * @return TaskLock object
     */
    TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock);

    /**
     * Release all locks & cleanup on shutdown
     */
    @PreDestroy
    void onDestroy();

}
