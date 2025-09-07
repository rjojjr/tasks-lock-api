package rjojjr.com.github.taskslock;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
abstract class StatefulTasksLockService implements TasksLockService {

    protected Set<TaskLock> taskLocks = new HashSet<>();
    protected final Object dbLock = new Object();
    private final Object cacheLock = new Object();

    @Override
    public List<TaskLock> getLocks(String contextId) {
        synchronized (cacheLock) {
            log.debug("getting locks for all tasks contextId: {}", contextId);
            return taskLocks.stream().toList();
        }
    }

    protected void cacheLock(TaskLock taskLock) {
        synchronized (cacheLock) {
            log.debug("adding lock for task {} to cache contextId: {}", taskLock.getTaskName(), taskLock.getContextId());
            taskLocks.add(taskLock);
        }
    }

    protected void removeLock(String taskName, String contextId) {
        synchronized (cacheLock) {
            log.debug("removing lock for task {} from cache contextId: {}", taskName, contextId);
            taskLocks = taskLocks.stream().filter(taskLock -> !taskLock.getTaskName().equals(taskName)).collect(Collectors.toSet());
        }
    }

    protected void removeLocks(String contextId) {
        synchronized (cacheLock) {
            log.debug("removing locks for all tasks from cache contextId: {}", contextId);
            taskLocks = new HashSet<>();
        }
    }
}
