package rjojjr.com.github.taskslock.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rjojjr.com.github.taskslock.TasksLockService;
import rjojjr.com.github.taskslock.models.TasksLockApiResponse;

import java.util.List;
import java.util.UUID;

@ConditionalOnProperty(name = "tasks-lock.api.enabled", havingValue = "true")
@RestController
@RequestMapping("/tasks-lock/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TasksLockApiController {

    private final TasksLockService tasksLockService;

    @GetMapping("/acquire")
    public TasksLockApiResponse acquire(@RequestParam String taskName, @RequestParam String contextId, @RequestParam(defaultValue = "true") Boolean waitForLock, HttpServletRequest request) {
        log.info("received acquire-lock request for task {} contextId: {}", taskName, contextId);
        var lock = tasksLockService.acquireLock(taskName, request.getRemoteHost(), contextId, waitForLock);
        var status = lock.getIsLocked() ? "lock acquired" : "lock not acquired";
        log.info("{} for request for task {} contextId {}", status, taskName, contextId);
        return new TasksLockApiResponse(taskName, lock.getContextId(), status, lock.getIsLocked(), lock.getLockedAt());
    }

    @GetMapping("/release")
    public TasksLockApiResponse release(@RequestParam String taskName) {
        log.info("received release-lock request for task {}", taskName);
        var contextId = tasksLockService.releaseLock(taskName);
        log.info("released lock request for task {} contextId: {}", taskName, contextId);
        return new TasksLockApiResponse(taskName, contextId, "lock released", false, null);
    }

    @GetMapping("/release/all")
    public TasksLockApiResponse releaseAll() {
        log.info("received release-all-locks request for all tasks");
        var contextId = tasksLockService.releaseLocks();
        log.info("released all locks request for all tasks contextId: {}", contextId);
        return new TasksLockApiResponse("all", contextId, "locks released", false, null);
    }

    @GetMapping
    public List<TasksLockApiResponse> listAll() {
        log.info("received release-list-all-locks request for all tasks");
        var contextId = UUID.randomUUID().toString();
        var locks = tasksLockService.getLocks(contextId)
                .stream()
                .map(lock -> new TasksLockApiResponse(lock.getTaskName(), lock.getContextId(), "locks", lock.getIsLocked(), lock.getLockedAt()))
                .toList();
        log.info("get all locks request for all tasks contextId: {}", contextId);
        return locks;
    }
}
