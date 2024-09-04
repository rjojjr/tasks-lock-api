package rjojjr.com.github.taskslock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import rjojjr.com.github.taskslock.entity.TaskLockEntity;
import rjojjr.com.github.taskslock.entity.TaskLockEntityRepository;
import rjojjr.com.github.taskslock.exception.AcquireLockFailureException;
import rjojjr.com.github.taskslock.exception.ReleaseLockFailureException;
import rjojjr.com.github.taskslock.exception.TasksLockShutdownFailure;
import rjojjr.com.github.taskslock.models.TaskLock;
import rjojjr.com.github.taskslock.util.HostUtil;
import rjojjr.com.github.taskslock.util.ThreadUtil;
import org.hibernate.exception.DataException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddedTasksLockService implements TasksLockService {

    private static final long RETRY_INTERVAL = 50;

    private final TaskLockEntityRepository taskLockEntityRepository;

    // TODO - Fetch lock status from here before querying the db
    private Set<TaskLock> taskLocks = new HashSet<>();
    private final Object releaseLock = new Object();

    @Override
    public TaskLock acquireLock(String taskName, String contextId, boolean waitForLock) {
        return acquireLock(taskName, HostUtil.getRemoteHost(), contextId, waitForLock);
    }

    @Override
    public TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock) {
        try {
            log.debug("Acquiring lock for task {}", taskName);
            synchronized (releaseLock) {
                // TODO - Synchronize at this level across module instances(maybe some kind of db table lock?)
                var lockedAt = new Date();
                var entity = taskLockEntityRepository.findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, hostName, contextId, new Date()));
                try {
                    if (!entity.getIsLocked()) {
                        entity.setIsLocked(true);
                        entity.setLockedAt(lockedAt);
                        entity.setIsLockedByHost(hostName);
                        entity.setContextId(contextId);
                        taskLockEntityRepository.save(entity);
                        var taskLock = new TaskLock(
                                taskName,
                                contextId,
                                true,
                                lockedAt,
                                () -> releaseLock(taskName)
                        );
                        taskLocks.add(taskLock);
                        log.debug("Task lock acquired for task {}", taskName);
                        return taskLock;
                    }
                } catch (DataException e) {
                    log.debug("Task lock not acquired for task {} because this worker lost in a race condition", taskName);
                }
            }

            if (waitForLock) {
                log.debug("Task lock not acquired for task {}, retrying in {}ms", taskName, RETRY_INTERVAL);
                ThreadUtil.sleep(RETRY_INTERVAL);
                return acquireLock(taskName, hostName, contextId, true);
            }
            log.debug("Task lock not acquired for task {}", taskName);
            return new TaskLock(taskName, contextId, false, null, () -> {});
        } catch (Exception e) {
            log.error("Error acquiring lock for task {}: {}", taskName, e.getMessage());
            throw new AcquireLockFailureException(taskName, e);
        }
    }

    @Override
    public void releaseLock(String taskName) {
        log.debug("Releasing lock for task {}", taskName);
        try {
            synchronized (releaseLock) {
                var entity = taskLockEntityRepository.findById(taskName).orElseGet(() -> new TaskLockEntity(taskName, false, null, null, new Date()));
                if (entity.getIsLocked()) {
                    entity.setIsLocked(false);
                    entity.setLockedAt(null);
                    entity.setIsLockedByHost(null);
                    entity.setContextId(null);

                    taskLockEntityRepository.save(entity);
                    taskLocks = taskLocks.stream().filter(taskLock -> !taskLock.getTaskName().equals(taskName)).collect(Collectors.toSet());
                }
            }
        } catch (Exception e) {
            log.error("Error releasing lock for task {}: {}", taskName, e.getMessage());
            throw new ReleaseLockFailureException(taskName, e);
        }
    }

    @Override
    public void onShutdown() {
        log.info("Shutting down TasksLockService and releasing task-locks");
        try {
            synchronized (releaseLock) {
                for(TaskLock lock : taskLocks) {
                    lock.getRelease().run();
                }
            }
            log.info("Shut down TasksLockService and released task-locks");
        } catch (Exception e) {
            log.error("error shutting down TasksLock API and releasing task-locks: {}", e.getMessage());
            throw new TasksLockShutdownFailure(e);
        }
    }

}
