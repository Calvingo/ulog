package com.ulog.backend.common.interceptor;

import com.ulog.backend.config.RateLimitProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RateLimitInterceptor.class);

    private final RateLimitProperties properties;
    
    // 存储每个IP的请求计数: key=ip:path, value=RequestCounter
    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();

    public RateLimitInterceptor(RateLimitProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        
        if (!properties.isEnabled()) {
            return true;
        }

        String clientIp = getClientIpAddress(request);
        String path = request.getRequestURI();
        String key = clientIp + ":" + path;

        // 获取该路径的限流配置
        int limitPerMinute = getRateLimitForPath(path);

        // 获取或创建计数器
        RequestCounter counter = requestCounts.computeIfAbsent(key, k -> new RequestCounter());

        // 检查是否超过限制
        if (!counter.allowRequest(limitPerMinute)) {
            log.warn("Rate limit exceeded for IP {} on path {}", clientIp, path);
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":429,\"message\":\"请求过于频繁，请稍后再试\"}");
            return false;
        }

        return true;
    }

    private int getRateLimitForPath(String path) {
        if (path.contains("/api/auth/login") || path.contains("/api/auth/register")) {
            return properties.getLoginPerMinute();
        } else if (path.contains("/api/ai") || path.contains("/api/deepseek")) {
            return properties.getAiPerMinute();
        } else {
            return properties.getDefaultPerMinute();
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 请求计数器（使用滑动窗口算法）
     */
    private static class RequestCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private long windowStart = System.currentTimeMillis();
        private static final long WINDOW_SIZE_MS = 60000; // 1分钟

        public synchronized boolean allowRequest(int limit) {
            long now = System.currentTimeMillis();
            
            // 如果超过1分钟，重置窗口
            if (now - windowStart >= WINDOW_SIZE_MS) {
                count.set(0);
                windowStart = now;
            }

            // 检查是否超过限制
            if (count.get() >= limit) {
                return false;
            }

            count.incrementAndGet();
            return true;
        }
    }
}

