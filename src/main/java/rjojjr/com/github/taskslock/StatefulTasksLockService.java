package rjojjr.com.github.taskslock;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor
abstract class StatefulTasksLockService implements TasksLockService {

    // TODO - Fetch lock status from here before querying the db
    protected Set<TaskLock> taskLocks = new HashSet<>();
    protected final Object dbLock = new Object();
    private final Object cacheLock = new Object();

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
}
