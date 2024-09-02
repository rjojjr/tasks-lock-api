package rjojjr.com.github.taskslock.entity;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Repository
public interface TaskLockEntityRepository extends JpaRepository<TaskLockEntity, String> {
}
