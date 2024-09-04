package rjojjr.com.github.taskslock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rjojjr.com.github.taskslock.exception.AcquireLockFailureException;
import rjojjr.com.github.taskslock.exception.ReleaseLockFailureException;
import rjojjr.com.github.taskslock.exception.TasksLockShutdownFailure;
import rjojjr.com.github.taskslock.models.TaskLock;
import rjojjr.com.github.taskslock.models.TasksLockApiResponse;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "true")
@Service
@RequiredArgsConstructor
@Slf4j
public class TasksLocksApiClientService implements TasksLockService {

    @Value("${tasks-lock.client.api-host:http://localhost:8080}")
    private String apiProtoAndHost;

    private final RestTemplate restTemplate;

    private Set<TaskLock> taskLocks = new HashSet<>();
    private final Object releaseLock = new Object();

    @Override
    public TaskLock acquireLock(String taskName, String contextId, boolean waitForLock) {
        try {
            var response = restTemplate.getForObject(String.format("%s/tasks-lock/api/v1/acquire?taskName=%s&contextId=%s&waitForLock=%s", apiProtoAndHost, taskName, contextId, waitForLock ? "true" : "false"), TasksLockApiResponse.class);
            if(!response.getIsLockAcquired()){
                return new TaskLock(taskName, contextId, false, null, () -> {});
            }
            var taskLock = new TaskLock(taskName, contextId, true, response.getLockedAt(), () -> releaseLock(taskName));
            synchronized (releaseLock) {
                taskLocks.add(taskLock);
            }
            return taskLock;
        } catch (Exception e) {
            log.error("Error acquiring lock from TasksLock API: {}", e.getMessage());
            throw new AcquireLockFailureException(taskName, e);
        }
    }

    @Override
    public void releaseLock(String taskName) {
        try {
            restTemplate.getForObject(String.format("%s/tasks-lock/api/v1/release?taskName=%s", apiProtoAndHost, taskName), TasksLockApiResponse.class);
            synchronized (releaseLock) {
                taskLocks = taskLocks.stream().filter(taskLock -> !taskLock.getTaskName().equals(taskName))
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("Error releasing lock from TasksLock API: {}", e.getMessage());
            throw new ReleaseLockFailureException(taskName, e);
        }
    }

    @Override
    public TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock){
        return acquireLock(taskName, contextId, waitForLock);
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
