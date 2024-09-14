package rjojjr.com.github.taskslock.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value
            = { RuntimeException.class })
    protected TasksLockApiResponse handleException(
            RuntimeException ex, WebRequest request) {
        if (ex instanceof TasksLockApiException) {
            if (ex instanceof ReleaseLockFailureException exception) {
                handleExceptionInternal(ex, exception.getMessage(),
                        new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
                log.warn("received error {} for {} returning {}", ex.getMessage(), request.getContextPath(), 500);
                return new TasksLockApiResponse(exception.getTaskName(), exception.getContextId(), ex.getMessage(), false, null);
            }
            if (ex instanceof AcquireLockFailureException exception) {
                handleExceptionInternal(ex, exception.getMessage(),
                        new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
                log.warn("received error {} for {} returning {}", ex.getMessage(), request.getContextPath(), 500);
                return new TasksLockApiResponse(exception.getTaskName(), exception.getContextId(), ex.getMessage(), false, null);
            }
        }
        handleExceptionInternal(ex, ex.getMessage(),
                new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
        log.warn("received error {} for {} returning {}", ex.getMessage(), request.getContextPath(), 500);
        return new TasksLockApiResponse(null, null, ex.getMessage(), false, null);
    }
}
