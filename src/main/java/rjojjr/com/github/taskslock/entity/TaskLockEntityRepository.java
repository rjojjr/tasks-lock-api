package rjojjr.com.github.taskslock.entity;

import jakarta.persistence.LockModeType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Repository
public interface TaskLockEntityRepository extends JpaRepository<TaskLockEntity, String> {

    Logger log = LoggerFactory.getLogger(TaskLockEntityRepository.class);

    /**
     * Attempts to acquire lock & locks 'task_locks' table while doing so.
     * A lock that has been held for longer than its timeout is treated as expired and may be re-acquired.
     * @param taskName unique task identifier.
     * @param hostName hostname of requesting service/container.
     * @param contextId a tracing ID provided by request.
     * @param timeoutMinutes minutes after which the acquired lock expires, or null for no expiry.
     * @param releaseLock Consumer function that releases lock.
     * @param cacheLock Consumer function that adds TaskLock object to cache.
     * @return resulting TaskLock object.
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    default TaskLock tryToAcquireLock(String taskName, String hostName, String contextId, Long timeoutMinutes, Consumer<String> releaseLock, Consumer<TaskLock> cacheLock){
        var lockedAt = new Date();

        var entity = findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, hostName, contextId, lockedAt, timeoutMinutes));
        if (entity.getIsLocked() && isExpired(entity, lockedAt)) {
            log.warn("lock for task {} held by host {} since {} exceeded its timeout of {} minutes, treating as expired contextId: {}",
                    taskName, entity.getIsLockedByHost(), entity.getLockedAt(), entity.getTimeoutMinutes(), entity.getContextId());
            entity.setIsLocked(false);
        }
        if (!entity.getIsLocked()) {
            entity.setIsLocked(true);
            entity.setLockedAt(lockedAt);
            entity.setIsLockedByHost(hostName);
            entity.setContextId(contextId);
            entity.setTimeoutMinutes(timeoutMinutes);
            save(entity);
            var taskLock = new TaskLock(
                    taskName,
                    contextId,
                    true,
                    lockedAt,
                    timeoutMinutes,
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
                timeoutMinutes,
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
        var entity = findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, null, null, new Date(), null));
        var contextId = entity.getContextId();
        if (entity.getIsLocked()) {
            entity.setIsLocked(false);
            entity.setLockedAt(null);
            entity.setIsLockedByHost(null);
            entity.setContextId(null);
            entity.setTimeoutMinutes(null);

             save(entity);
        }
        flush();
        return contextId;
    }

    /**
     * Removes lock for all tasks
     * @return contextId
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    default String releaseLocks(){
        var contextId = UUID.randomUUID().toString();
        findAll().forEach(entity -> {
            if (entity.getIsLocked()) {
                entity.setIsLocked(false);
                entity.setLockedAt(null);
                entity.setIsLockedByHost(null);
                entity.setContextId(null);

                save(entity);
            }
        });

        flush();
        return contextId;
    }

    /**
     * @return true if the entity's lock has been held longer than its timeout as of {@code now}.
     * Locks without a timeout or without a lockedAt timestamp never expire.
     */
    private static boolean isExpired(TaskLockEntity entity, Date now) {
        if (entity.getTimeoutMinutes() == null || entity.getLockedAt() == null) {
            return false;
        }
        var expiresAt = entity.getLockedAt().getTime() + TimeUnit.MINUTES.toMillis(entity.getTimeoutMinutes());
        return now.getTime() >= expiresAt;
    }
}
