package rjojjr.com.github.taskslock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import rjojjr.com.github.taskslock.entity.TaskLockEntityRepository;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmbeddedTasksLockServiceTimeoutTests {

    private static final String TASK = "timeout-test-task";

    @Autowired
    private TasksLockService tasksLockService;

    @Autowired
    private TaskLockEntityRepository entityRepository;

    @AfterEach
    void cleanUp() {
        tasksLockService.releaseLock(TASK);
    }

    @Test
    void acquireLockUsesDefaultTimeoutWhenNoneGiven() {
        var lock = tasksLockService.acquireLock(TASK, "ctx-1", false);

        assertThat(lock.getIsLocked()).isTrue();
        assertThat(lock.getTimeoutMinutes()).isEqualTo(60L);
        assertThat(entityRepository.findById(TASK)).get()
                .satisfies(entity -> assertThat(entity.getTimeoutMinutes()).isEqualTo(60L));
    }

    @Test
    void acquireLockStoresExplicitTimeout() {
        var lock = tasksLockService.acquireLock(TASK, "ctx-1", false, 5L);

        assertThat(lock.getIsLocked()).isTrue();
        assertThat(lock.getTimeoutMinutes()).isEqualTo(5L);
        assertThat(entityRepository.findById(TASK)).get()
                .satisfies(entity -> assertThat(entity.getTimeoutMinutes()).isEqualTo(5L));
    }

    @Test
    void heldLockWithinTimeoutIsNotReacquired() {
        tasksLockService.acquireLock(TASK, "ctx-1", false, 5L);

        var second = tasksLockService.acquireLock(TASK, "ctx-2", false, 5L);

        assertThat(second.getIsLocked()).isFalse();
        assertThat(entityRepository.findById(TASK)).get()
                .satisfies(entity -> assertThat(entity.getContextId()).isEqualTo("ctx-1"));
    }

    @Test
    void expiredLockCanBeAcquiredByAnotherRequester() {
        tasksLockService.acquireLock(TASK, "ctx-1", false, 5L);
        backdateLock(6);

        var second = tasksLockService.acquireLock(TASK, "ctx-2", false, 10L);

        assertThat(second.getIsLocked()).isTrue();
        assertThat(second.getTimeoutMinutes()).isEqualTo(10L);
        assertThat(entityRepository.findById(TASK)).get().satisfies(entity -> {
            assertThat(entity.getIsLocked()).isTrue();
            assertThat(entity.getContextId()).isEqualTo("ctx-2");
            assertThat(entity.getTimeoutMinutes()).isEqualTo(10L);
        });
    }

    @Test
    void waitingRequesterAcquiresLockOnceItExpires() {
        tasksLockService.acquireLock(TASK, "ctx-1", false, 1L);
        backdateLock(2);

        // Would block forever if expiry were not honoured.
        var second = tasksLockService.acquireLock(TASK, "ctx-2", true);

        assertThat(second.getIsLocked()).isTrue();
        assertThat(second.getContextId()).isEqualTo("ctx-2");
    }

    @Test
    void legacyLockWithoutTimeoutNeverExpires() {
        tasksLockService.acquireLock(TASK, "ctx-1", false);
        var entity = entityRepository.findById(TASK).orElseThrow();
        entity.setTimeoutMinutes(null);
        entity.setLockedAt(minutesAgo(600));
        entityRepository.saveAndFlush(entity);

        var second = tasksLockService.acquireLock(TASK, "ctx-2", false);

        assertThat(second.getIsLocked()).isFalse();
    }

    @Test
    void releaseClearsTimeout() {
        tasksLockService.acquireLock(TASK, "ctx-1", false, 5L);

        tasksLockService.releaseLock(TASK);

        assertThat(entityRepository.findById(TASK)).get().satisfies(entity -> {
            assertThat(entity.getIsLocked()).isFalse();
            assertThat(entity.getTimeoutMinutes()).isNull();
        });
    }

    private void backdateLock(int minutes) {
        var entity = entityRepository.findById(TASK).orElseThrow();
        entity.setLockedAt(minutesAgo(minutes));
        entityRepository.saveAndFlush(entity);
    }

    private static Date minutesAgo(int minutes) {
        return new Date(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(minutes));
    }
}
