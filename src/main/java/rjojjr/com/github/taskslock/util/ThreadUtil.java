package rjojjr.com.github.taskslock.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ThreadUtil {

    public static void sleep(long millis){
        try{
            Thread.sleep(millis);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
