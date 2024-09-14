package rjojjr.com.github.taskslock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rjojjr.com.github.taskslock.exception.AcquireLockFailureException;
import rjojjr.com.github.taskslock.exception.ReleaseLockFailureException;
import rjojjr.com.github.taskslock.models.TaskLock;
import rjojjr.com.github.taskslock.models.TasksLockApiResponse;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "true")
@Service
@Slf4j
public class TasksLocksApiClientService extends DestroyableTasksLockService {

    @Value("${tasks-lock.client.api-host:http://localhost:8080}")
    private String apiProtoAndHost;

    private final RestTemplate restTemplate;

    @Autowired
    public TasksLocksApiClientService(RestTemplate restTemplate) {
        super();
        this.restTemplate = restTemplate;
    }

    @Override
    public TaskLock acquireLock(String taskName, String contextId, boolean waitForLock) {
        try {
            log.debug("attempting to acquire lock for task {}, waiting for lock: {} contextId: {}", taskName, waitForLock, contextId);
            var response = restTemplate.getForObject(String.format("%s/tasks-lock/api/v1/acquire?taskName=%s&contextId=%s&waitForLock=%s", apiProtoAndHost, taskName, contextId, waitForLock ? "true" : "false"), TasksLockApiResponse.class);
            if(!response.getIsLockAcquired()){
                log.debug("did not acquire lock for task {} contextId: {}", taskName, contextId);
                return new TaskLock(taskName, contextId, false, null, () -> {});
            }
            var taskLock = new TaskLock(taskName, contextId, true, response.getLockedAt(), () -> releaseLock(taskName));
            cacheLock(taskLock);
            log.debug("acquired lock for task {} contextId: {}", taskName, contextId);
            return taskLock;
        } catch (Exception e) {
            log.error("error acquiring lock from TasksLock API: {}", e.getMessage());
            throw new AcquireLockFailureException(taskName, contextId, e);
        }
    }

    @Override
    public String releaseLock(String taskName) {
        try {

            log.debug("attempting to release lock for task {}", taskName);
            var response = restTemplate.getForObject(String.format("%s/tasks-lock/api/v1/release?taskName=%s", apiProtoAndHost, taskName), TasksLockApiResponse.class);

            assert response != null;
            var contextId = response.getContextId();
            removeLock(response.getTaskName(), contextId);
            log.debug("released lock for task {}, contextId: {}", taskName, contextId);
            return contextId;
        } catch (Exception e) {
            log.error("error releasing lock from TasksLock API: {}", e.getMessage());
            throw new ReleaseLockFailureException(taskName, null, e);
        }
    }

    @Override
    public TaskLock acquireLock(String taskName, String hostName, String contextId, boolean waitForLock){
        return acquireLock(taskName, contextId, waitForLock);
    }


}
