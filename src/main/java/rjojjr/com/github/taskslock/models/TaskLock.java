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
    private Runnable release;
}
