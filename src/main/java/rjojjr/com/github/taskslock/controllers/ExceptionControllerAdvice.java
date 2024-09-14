package rjojjr.com.github.taskslock.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import rjojjr.com.github.taskslock.exception.AcquireLockFailureException;
import rjojjr.com.github.taskslock.exception.ReleaseLockFailureException;
import rjojjr.com.github.taskslock.exception.TasksLockApiException;
import rjojjr.com.github.taskslock.models.TasksLockApiResponse;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "tasks-lock.api.enabled", havingValue = "true")
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { TasksLockApiException.class })
    protected TasksLockApiResponse handleApiException(
            RuntimeException ex, WebRequest request) {
        log.warn("received error {} for {} returning {}", ex.getMessage(), request.getContextPath(), 500);
        if (ex instanceof ReleaseLockFailureException exception) {
            handleExceptionInternal(ex, exception.getMessage(),
                    new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
            return new TasksLockApiResponse(exception.getTaskName(), exception.getContextId(), ex.getMessage(), false, null);
        }
        if (ex instanceof AcquireLockFailureException exception) {
            handleExceptionInternal(ex, exception.getMessage(),
                    new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
            return new TasksLockApiResponse(exception.getTaskName(), exception.getContextId(), ex.getMessage(), false, null);
        }
        handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
        return new TasksLockApiResponse(null, null, ex.getMessage(), false, null);
    }

    @ExceptionHandler(value
            = { RuntimeException.class })
    protected TasksLockApiResponse handleException(
            RuntimeException ex, WebRequest request) {
        log.warn("received error {} for {} returning {}", ex.getMessage(), request.getContextPath(), 500);
        handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
        return new TasksLockApiResponse(null, null, ex.getMessage(), false, null);
    }
}
