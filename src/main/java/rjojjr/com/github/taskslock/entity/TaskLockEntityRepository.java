package rjojjr.com.github.taskslock.entity;

import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.DataException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.Date;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Repository
public interface TaskLockEntityRepository extends JpaRepository<TaskLockEntity, String> {

    @Lock(LockModeType.OPTIMISTIC)
    default TaskLock acquireLock(String taskName, String hostName, String contextId, Consumer<String> releaseLock, Consumer<TaskLock> cacheLock){
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
        return null;
    }
}
