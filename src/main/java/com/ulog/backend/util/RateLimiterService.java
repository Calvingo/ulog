package com.ulog.backend.util;

import com.ulog.backend.common.exception.RateLimitException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class RateLimiterService {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    public void checkRate(String key, int maxRequests, Duration window) {
        long now = Instant.now().toEpochMilli();
        Deque<Long> queue = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (queue) {
            while (!queue.isEmpty() && now - queue.peekFirst() > window.toMillis()) {
                queue.pollFirst();
            }
            if (queue.size() >= maxRequests) {
                throw new RateLimitException("too many requests");
            }
            queue.addLast(now);
        }
    }
}
