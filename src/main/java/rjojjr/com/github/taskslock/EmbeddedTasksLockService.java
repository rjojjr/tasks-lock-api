package rjojjr.com.github.taskslock;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import rjojjr.com.github.taskslock.entity.TaskLockEntity;
import rjojjr.com.github.taskslock.entity.TaskLockEntityRepository;
import rjojjr.com.github.taskslock.exception.AcquireLockFailureException;
import rjojjr.com.github.taskslock.exception.ReleaseLockFailureException;
import rjojjr.com.github.taskslock.models.TaskLock;
import rjojjr.com.github.taskslock.util.HostUtil;
import rjojjr.com.github.taskslock.util.ThreadUtil;
import org.hibernate.exception.DataException;
import java.util.Date;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Service
@Slf4j
public class EmbeddedTasksLockService extends DestroyableTasksLockService {

    private static final long RETRY_INTERVAL = 50;

    private final TaskLockEntityRepository taskLockEntityRepository;

    @Autowired
    public EmbeddedTasksLockService(TaskLockEntityRepository taskLockEntityRepository) {
        this.taskLockEntityRepository = taskLockEntityRepository;
    }

    @Override
    @Transactional
    public TaskLock acquireLock(String taskName, String contextId, boolean waitForLock) {
        return acquireLock(taskName, HostUtil.getRemoteHost(), contextId, waitForLock);
    }

    @Override
    @Transactional
    public TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock) {
        try {
            log.debug("attempting to acquire lock for task {}, waiting for lock: {} contextId: {}", taskName, waitForLock, contextId);
            synchronized (dbLock) {
                try {
                    var taskLock = taskLockEntityRepository.acquireLock(taskName, hostName, contextId, this::releaseLock, this::cacheLock);
                    if (taskLock != null) {
                        log.debug("acquired lock for task {} contextId: {}", taskName, contextId);
                        return taskLock;
                    }
                } catch (DataException e) {
                    log.debug("Task lock not acquired for task {} because this worker lost in a race condition", taskName);
                }
            }

            if (waitForLock) {
                log.debug("task lock not acquired for task {}, retrying in {}ms contextId: {}", taskName, RETRY_INTERVAL, contextId);
                ThreadUtil.sleep(RETRY_INTERVAL);
                return acquireLock(taskName, hostName, contextId, true);
            }
            log.debug("did not acquire lock for task {} contextId: {}", taskName, contextId);
            return new TaskLock(taskName, contextId, false, null, () -> {});
        } catch (Exception e) {
            log.error("error acquiring lock for task {}: {} contextId: {}", taskName, e.getMessage(), contextId);
            throw new AcquireLockFailureException(taskName, contextId, e);
        }
    }

    @Override
    public String releaseLock(String taskName) {
        log.debug("attempting to release lock for task {}", taskName);
        try {
            String contextId;
            synchronized (dbLock) {
                var entity = taskLockEntityRepository.findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, null, null, new Date()));
                contextId = entity.getContextId();
                if (entity.getIsLocked()) {
                    entity.setIsLocked(false);
                    entity.setLockedAt(null);
                    entity.setIsLockedByHost(null);
                    entity.setContextId(null);

                    taskLockEntityRepository.save(entity);
                }
            }
            removeLock(taskName, contextId);
            log.debug("released lock for task {}, contextId: {}", taskName, contextId);
            return contextId;
        } catch (Exception e) {
            log.error("error releasing lock for task {}: {}", taskName, e.getMessage());
            throw new ReleaseLockFailureException(taskName, null, e);
        }
    }
}
