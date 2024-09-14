package rjojjr.com.github.taskslock.entity;

import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.Date;
import java.util.function.Consumer;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Repository
public interface TaskLockEntityRepository extends JpaRepository<TaskLockEntity, String> {

    /**
     * Attempts to acquire lock & locks 'task_locks' table while doing so.
     * @param taskName unique task identifier.
     * @param hostName hostname of requesting service/container.
     * @param contextId a tracing ID provided by request.
     * @param releaseLock Consumer function that releases lock.
     * @param cacheLock Consumer function that adds TaskLock object to cache.
     * @return resulting TaskLock object.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    default TaskLock tryToAcquireLock(String taskName, String hostName, String contextId, Consumer<String> releaseLock, Consumer<TaskLock> cacheLock){
        var lockedAt = new Date();

        var entity = findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, hostName, contextId, new Date()));
        if (!entity.getIsLocked()) {
            entity.setIsLocked(true);
            entity.setLockedAt(lockedAt);
            entity.setIsLockedByHost(hostName);
            entity.setContextId(contextId);
            save(entity);
            var taskLock = new TaskLock(
                    taskName,
                    contextId,
                    true,
                    lockedAt,
                    () -> releaseLock.accept(taskName)
            );
            cacheLock.accept(taskLock);
            return taskLock;
        }
        flush();
        return new TaskLock(
                taskName,
                contextId,
                false,
                lockedAt,
                () -> {}
        );
    }

    /**
     * Removes lock for task if it exists
     * @param taskName unique task identifier
     * @return contextId
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    default String releaseLock(String taskName){
        var entity = findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, null, null, new Date()));
        var contextId = entity.getContextId();
        if (entity.getIsLocked()) {
            entity.setIsLocked(false);
            entity.setLockedAt(null);
            entity.setIsLockedByHost(null);
            entity.setContextId(null);

             save(entity);
        }
        flush();
        return contextId;
    }
}
