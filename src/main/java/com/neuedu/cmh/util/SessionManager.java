package com.neuedu.cmh.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
public class SessionManager {
    private static final Map<String, String> CODE_CACHE = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public static void saveCode(String sessionId, String code) {
        CODE_CACHE.put(sessionId, code);
        // 5分钟后自动清除
        executor.schedule(() -> CODE_CACHE.remove(sessionId), 5, TimeUnit.MINUTES);
    }

    public static String getCode(String sessionId) {
        return CODE_CACHE.get(sessionId);
    }

    public static void removeCode(String sessionId) {
        CODE_CACHE.remove(sessionId);
    }


}


