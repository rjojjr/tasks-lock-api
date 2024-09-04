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

@ConditionalOnProperty(name = "tasks-lock.api.enabled", havingValue = "true")
@RestController
@RequestMapping("/tasks-lock/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TasksLockApiController {

    private final TasksLockService tasksLockService;

    @GetMapping("/acquire")
    public TasksLockApiResponse acquire(@RequestParam String taskName, @RequestParam String contextId, @RequestParam(defaultValue = "true") Boolean waitForLock, HttpServletRequest request) {
        var lock = tasksLockService.acquireLock(taskName, request.getRemoteHost(), contextId, waitForLock);
        return new TasksLockApiResponse(taskName, lock.getIsLocked(), lock.getLockedAt());
    }

    @GetMapping("/release")
    public TasksLockApiResponse release(@RequestParam String taskName) {
        tasksLockService.releaseLock(taskName);
        return new TasksLockApiResponse(taskName, false, null);
    }
}
