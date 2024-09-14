package rjojjr.com.github.taskslock;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import rjojjr.com.github.taskslock.exception.TasksLockShutdownFailure;
import rjojjr.com.github.taskslock.models.TaskLock;

@Slf4j
abstract class DestroyableTasksLockService extends StatefulTasksLockService {

    public DestroyableTasksLockService() {
        super();
    }

    @PreDestroy
    @Override
    public void onDestroy() {
        log.info("Shutting down TasksLockService and releasing task-locks");
        try {
            synchronized (dbLock) {
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
