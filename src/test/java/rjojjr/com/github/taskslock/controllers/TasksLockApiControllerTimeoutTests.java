package rjojjr.com.github.taskslock.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import rjojjr.com.github.taskslock.TasksLockService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TasksLockApiControllerTimeoutTests {

    private static final String TASK = "controller-timeout-test-task";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TasksLockService tasksLockService;

    @AfterEach
    void cleanUp() {
        tasksLockService.releaseLock(TASK);
    }

    @Test
    void acquireWithoutTimeoutParamReturnsDefault() throws Exception {
        mockMvc.perform(get("/tasks-lock/api/v1/acquire")
                        .param("taskName", TASK)
                        .param("contextId", "ctx-1")
                        .param("waitForLock", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLockAcquired").value(true))
                .andExpect(jsonPath("$.timeoutMinutes").value(60));
    }

    @Test
    void acquireWithTimeoutParamEchoesIt() throws Exception {
        mockMvc.perform(get("/tasks-lock/api/v1/acquire")
                        .param("taskName", TASK)
                        .param("contextId", "ctx-1")
                        .param("waitForLock", "false")
                        .param("timeoutMinutes", "15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isLockAcquired").value(true))
                .andExpect(jsonPath("$.timeoutMinutes").value(15));
    }

    @Test
    void listIncludesTimeout() throws Exception {
        tasksLockService.acquireLock(TASK, "ctx-1", false, 15L);

        mockMvc.perform(get("/tasks-lock/api/v1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.taskName == '" + TASK + "')].timeoutMinutes").value(15));
    }
}
