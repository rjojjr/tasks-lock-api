package rjojjr.com.github.taskslock;

import jakarta.annotation.PreDestroy;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.List;

public interface TasksLockService {

    /**
     * Acquire lock for task using the default lock timeout
     * @param taskName unique task identifier
     * @param contextId a tracing identifier provided by the consumer
     * @param waitForLock block until lock is acquired
     * @return TaskLock object
     */
    TaskLock acquireLock(String taskName, String contextId, boolean waitForLock);

    /**
     * Acquire lock for task
     * @param taskName unique task identifier
     * @param contextId a tracing identifier provided by the consumer
     * @param waitForLock block until lock is acquired
     * @param timeoutMinutes number of minutes the lock stays valid before it is considered expired
     *                       and may be acquired by another requester, or null to use the default(60 minutes)
     * @return TaskLock object
     */
    TaskLock acquireLock(String taskName, String contextId, boolean waitForLock, Long timeoutMinutes);

    /**
     * Release lock for task
     * @param taskName unique task identifier
     * @return contextId a tracing identifier provided by the consumer
     */
    String releaseLock(String taskName);

    /**
     * Release locks for all tasks
     * @return contextId a tracing identifier provided by the consumer
     */
    String releaseLocks();

    /**
     * Acquire lock with embedded impl. using the default lock timeout
     * @param taskName unique task identifier
     * @param hostName hostname of application/container acquiring lock
     * @param contextId a tracing identifier provided by the consumer
     * @param waitForLock block until lock is acquired
     * @return TaskLock object
     */
    TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock);

    /**
     * Acquire lock with embedded impl.
     * @param taskName unique task identifier
     * @param hostName hostname of application/container acquiring lock
     * @param contextId a tracing identifier provided by the consumer
     * @param waitForLock block until lock is acquired
     * @param timeoutMinutes number of minutes the lock stays valid before it is considered expired
     *                       and may be acquired by another requester, or null to use the default(60 minutes)
     * @return TaskLock object
     */
    TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock, Long timeoutMinutes);

    /**
     * List locks for all tasks
     * @param contextId a tracing identifier provided by the consumer
     * @return List<TaskLock> locks
     */
    List<TaskLock> getLocks(String contextId);

    /**
     * Release all locks & cleanup on shutdown
     */
    @PreDestroy
    void onDestroy();

}
