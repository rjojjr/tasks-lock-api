package rjojjr.com.github.taskslock.autoconfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import rjojjr.com.github.taskslock.EmbeddedTasksLockService;
import rjojjr.com.github.taskslock.TasksLockService;
import rjojjr.com.github.taskslock.entity.TaskLockRepository;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "false", matchIfMissing = true)
@Configuration
@EntityScan({"rjojjr.com.github.taskslock.entity"})
@EnableJpaRepositories({"rjojjr.com.github.taskslock.entity"})
@ComponentScan({
        "rjojjr.com.github.taskslock.**"
})
public class EmbeddedTasksLockConfiguration {

    @Autowired
    private TaskLockRepository taskLockRepository;

    @Bean
    public TasksLockService tasksLockService() {
        return new EmbeddedTasksLockService(taskLockRepository);
    }
}
