package rjojjr.com.github.taskslock.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.DataException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
public class TaskLockRepository {

    private final TaskLockEntityRepository taskLockEntityRepository;

    @Transactional
    public TaskLock getTaskLock(String taskName, String hostName, String contextId, Consumer<String> releaseLock, Consumer<TaskLock> cacheLock) {
        try {
            var taskLock = taskLockEntityRepository.tryToAcquireLock(taskName, hostName, contextId, releaseLock, cacheLock);
            if (taskLock.getIsLocked()) {
                log.debug("acquired lock for task {} contextId: {}", taskName, contextId);
                return taskLock;
            }
        } catch (DataException e) {
            log.debug("Task lock not acquired for task {} because this worker lost in a race condition", taskName);
        }
        return null;
    }

    @Transactional
    public String releaseLock(String taskName) {
        return taskLockEntityRepository.releaseLock(taskName);
    }
}
