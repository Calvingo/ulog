package com.ulog.backend.compliance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ComplianceCleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(ComplianceCleanupScheduler.class);

    private final OperationLogService operationLogService;

    @Value("${logging.retention.days:180}")
    private int retentionDays;

    public ComplianceCleanupScheduler(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    /**
     * 每天凌晨2点清理旧日志
     * 保留期由配置文件决定，默认180天（6个月）
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        log.info("Starting scheduled cleanup of old logs, retention: {} days", retentionDays);
        
        try {
            int deletedCount = operationLogService.cleanupOldLogs(retentionDays);
            log.info("Scheduled log cleanup completed, deleted {} records", deletedCount);
        } catch (Exception e) {
            log.error("Error during scheduled log cleanup", e);
        }
    }
}

