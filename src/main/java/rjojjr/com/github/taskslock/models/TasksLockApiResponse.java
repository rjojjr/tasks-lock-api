package rjojjr.com.github.taskslock.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TasksLockApiResponse {
    private String taskName;
    private Boolean isLockAcquired;
    private Date lockedAt;
}
