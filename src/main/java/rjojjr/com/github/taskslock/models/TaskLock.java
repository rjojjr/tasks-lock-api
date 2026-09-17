package rjojjr.com.github.taskslock.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TaskLock {
    private String taskName;
    private String contextId;
    private Boolean isLocked;
    private Date lockedAt;
    /**
     * Number of minutes after {@link #lockedAt} at which the lock is considered
     * expired and may be acquired by another requester. {@code null} means the
     * lock never expires.
     */
    private Long timeoutMinutes;
    private Runnable release;
}
