package com.ulog.backend.compliance.service;

import com.ulog.backend.domain.compliance.OperationLog;
import com.ulog.backend.repository.OperationLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
public class OperationLogService {

    private static final Logger log = LoggerFactory.getLogger(OperationLogService.class);

    private final OperationLogRepository logRepository;

    public OperationLogService(OperationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * 记录操作日志（异步）
     */
    @Async
    @Transactional
    public void logOperation(Long userId, String operationType, String operationDetail) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            OperationLog logEntry = new OperationLog();
            logEntry.setUserId(userId);
            logEntry.setOperationType(operationType);
            logEntry.setOperationDetail(operationDetail);
            
            if (request != null) {
                logEntry.setIpAddress(getClientIpAddress(request));
                logEntry.setUserAgent(request.getHeader("User-Agent"));
                logEntry.setRequestUri(request.getRequestURI());
                logEntry.setHttpMethod(request.getMethod());
            }

            logRepository.save(logEntry);
            log.debug("Operation logged: type={}, user={}", operationType, userId);
        } catch (Exception e) {
            log.error("Failed to log operation", e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 记录操作日志（带请求信息）
     */
    @Async
    @Transactional
    public void logOperation(Long userId, String operationType, String operationDetail,
                           HttpServletRequest request, Integer statusCode) {
        try {
            OperationLog logEntry = new OperationLog();
            logEntry.setUserId(userId);
            logEntry.setOperationType(operationType);
            logEntry.setOperationDetail(operationDetail);
            logEntry.setStatusCode(statusCode);
            
            if (request != null) {
                logEntry.setIpAddress(getClientIpAddress(request));
                logEntry.setUserAgent(request.getHeader("User-Agent"));
                logEntry.setRequestUri(request.getRequestURI());
                logEntry.setHttpMethod(request.getMethod());
            }

            logRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to log operation", e);
        }
    }

    /**
     * 记录操作日志（带错误信息）
     */
    @Async
    @Transactional
    public void logOperationWithError(Long userId, String operationType, String operationDetail,
                                     String errorMessage, Integer statusCode) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            OperationLog logEntry = new OperationLog();
            logEntry.setUserId(userId);
            logEntry.setOperationType(operationType);
            logEntry.setOperationDetail(operationDetail);
            logEntry.setErrorMessage(errorMessage);
            logEntry.setStatusCode(statusCode);
            
            if (request != null) {
                logEntry.setIpAddress(getClientIpAddress(request));
                logEntry.setUserAgent(request.getHeader("User-Agent"));
                logEntry.setRequestUri(request.getRequestURI());
                logEntry.setHttpMethod(request.getMethod());
            }

            logRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to log operation with error", e);
        }
    }

    /**
     * 获取用户操作日志
     */
    @Transactional(readOnly = true)
    public List<OperationLog> getUserLogs(Long userId) {
        return logRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * 获取特定类型的操作日志
     */
    @Transactional(readOnly = true)
    public List<OperationLog> getLogsByType(String operationType) {
        return logRepository.findByOperationTypeOrderByCreatedAtDesc(operationType);
    }

    /**
     * 清理旧日志（保留180天）
     */
    @Transactional
    public int cleanupOldLogs(int retentionDays) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = logRepository.deleteOldLogs(cutoffDate);
        log.info("Cleaned up {} old operation logs before {}", deletedCount, cutoffDate);
        return deletedCount;
    }

    /**
     * 获取当前HTTP请求
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

