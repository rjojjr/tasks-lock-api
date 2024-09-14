# TasksLock API README

The 'TasksLock API' is a Springboot module/system meant to help synchronize 
work in a HA environment. It is useful for enforcing a limit on 
jobs that should only be run once at the same time.

## Module Modes

The 'TasksLock API' Springboot module has three modes:

- Embedded Mode
- API Mode
- API Client Mode

### Embedded Mode

Embedded mode is meant for cases when you want your target Springboot
application to handle all the database configuration.

To run this module in Embedded Mode, all you have to do is import
this module as dependency to your target module.

### API Mode

This module can be ran as a standalone API incase you want to centralize/segregate 
this functionality.

To run this module in API Mode, you must either import this module as a dependency
of another Springboot app, or stand the Java Artifact up on its own, and set the `tasks-lock.api.enabled` env. var./configuration 
property to `true`.

#### Docker API

You can run the Tasks Lock API as a docker container by building it with the included [Dockerfile](Dockerfile). 
The container can be run by setting the proper SQL DB environment variables.

#### Required SQL Database Environment Variables

When this module is run in API mode, the following environment variables/application properties
must be set with the appropriate values for the SQL datastore instance you intend to use:

- `spring.jpa.database-platform`
- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

##### SQL Schema Generation

The API will generate and execute the SQL schema needed for the app to operate,
there is nothing you need to do in this regard.

### API Client Mode

Use this mode in your target module impl. when you have a central
instance of this module in API Mode that you want to consume from.

To run this module in API Client Mode, you must import this module as a dependency
of your target Springboot app, and set the `tasks-lock.client.enabled` env. var./configuration
property to `true`. You must also set the `tasks-lock.client.api-host` property
to the protocol and hostname of the API Mode module instance(`http://localhost:8080`).

## Consuming Tasks Locks

To consume the TasksLock API, you simply need to inject the `rjojjr.com.github.taskslock.TasksLockService`
component into the target class and call the appropriate methods.

**EX:**

```java
package some.app.components;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import rjojjr.com.github.taskslock.TasksLockService;

@Component
public class SomeComponent {
    
    private final TasksLockService tasksLockService;
    
    @Autowired
    public SomeComponent(TasksLockService tasksLockService) {
        this.tasksLockService = tasksLockService;
    }
    
    // Don't wait for lock
    public void doSomethingSynchronouslyWithoutWaitingForLock(){
        var taskLock = this.tasksLockService.acquireLock("someUniqueTaskName", "someContextId", false);
        if(taskLock.getIsLocked()) {
            // Lock acquired, do something and release lock
            ...
            taskLock.getRelease().run();
        } else {
            // Did not acquire lock, do something else(?)
            ...
        }
    }

    // Wait for lock
    public void doSomethingSynchronouslyAndWaitForLock(){
        // Blocks until lock is acquired
        var taskLock = this.tasksLockService.acquireLock("someUniqueTaskName", "someContextId", true);
        // No need to check `isLocked` because this method will not finish unless it either
        // acquires the lock or throws a RuntimeException for some unexpected reason
        
        // Lock acquired, do something and release lock
        ...
        taskLock.getRelease().run();
    }
}
```

## Building the Module

To build the module artifact with the included Gradle Wrapper,
simply run the `bootJar` gradle task:

```shell
./gradlew bootJar
```