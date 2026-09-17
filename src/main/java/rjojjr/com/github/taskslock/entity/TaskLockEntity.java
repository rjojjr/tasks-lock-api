package rjojjr.com.github.taskslock.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "task_locks")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskLockEntity {
    @Id
    @Column(columnDefinition = "VARCHAR(256)", unique = true, nullable = false)
    private String taskName;

    @Column(nullable = false)
    private Boolean isLocked;

    @Column(columnDefinition = "VARCHAR(256)")
    private String isLockedByHost;

    @Column(columnDefinition = "VARCHAR(256)")
    private String contextId;

    private Date lockedAt;

    /**
     * Number of minutes after {@code lockedAt} at which the lock is considered expired.
     * {@code null} means the lock never expires (legacy rows written before this column existed).
     */
    private Long timeoutMinutes;
}
