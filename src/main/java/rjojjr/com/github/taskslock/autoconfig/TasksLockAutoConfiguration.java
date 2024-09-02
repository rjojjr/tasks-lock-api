package rjojjr.com.github.taskslock.autoconfig;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({
        "rjojjr.com.github.taskslock",
        "rjojjr.com.github.taskslock.autoconfig"
})
public class TasksLockAutoConfiguration {
}
