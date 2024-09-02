package rjojjr.com.github.taskslock.autoconfig;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import rjojjr.com.github.taskslock.TasksLockService;
import rjojjr.com.github.taskslock.TasksLocksApiClientService;

@ConditionalOnProperty(name = "tasks-lock.client.enabled", havingValue = "true")
@Configuration
public class ApiClientTasksLockConfiguration {

    @Bean
    public TasksLockService tasksLockService() {
        return new TasksLocksApiClientService(new RestTemplate());
    }
}
