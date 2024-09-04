package rjojjr.com.github.taskslock.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import rjojjr.com.github.taskslock.exception.TasksLockApiException;

import java.net.InetAddress;
import java.net.UnknownHostException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostUtil {
    public static String getRemoteHost() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new TasksLockApiException(e.getMessage(), e);
        }
    }
}
