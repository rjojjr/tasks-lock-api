package rjojjr.com.github.taskslock.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.InetAddress;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HostUtil {
    public static String getRemoteHost(){
        return InetAddress.getLoopbackAddress().getHostName();
    }
}
