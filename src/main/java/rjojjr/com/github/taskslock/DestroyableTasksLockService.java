package rjojjr.com.github.taskslock;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import rjojjr.com.github.taskslock.exception.TasksLockShutdownFailure;
import rjojjr.com.github.taskslock.models.TaskLock;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Getter
@NoArgsConstructor
public abstract class DestroyableTasksLockService implements TasksLockService {

    // TODO - Fetch lock status from here before querying the db
    protected Set<TaskLock> taskLocks = new HashSet<>();
    protected final Object releaseLock = new Object();

    @Override
    public void onDestroy() {
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
