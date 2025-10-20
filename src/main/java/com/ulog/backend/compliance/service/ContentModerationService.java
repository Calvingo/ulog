package com.ulog.backend.compliance.service;

import com.ulog.backend.compliance.dto.ModerationResult;
import com.ulog.backend.config.ContentModerationProperties;
import com.ulog.backend.domain.compliance.ContentModerationLog;
import com.ulog.backend.repository.ContentModerationLogRepository;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentModerationService {

    private static final Logger log = LoggerFactory.getLogger(ContentModerationService.class);

    private final ContentModerationProperties properties;
    private final ContentModerationLogRepository logRepository;

    // 本地敏感词库（基础版，实际应用中应该更全面）
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
        // 政治敏感词
        "暴力革命", "推翻政府", "分裂国家", "法轮功", "达赖", "台独", "藏独", "疆独", "港独",
        // 色情低俗词汇
        "色情", "淫秽", "裸体", "性交", "卖淫", "嫖娼",
        // 暴力血腥
        "杀人", "自杀", "恐怖袭击", "爆炸", "血腥",
        // 赌博相关
        "赌博", "赌场", "博彩", "六合彩",
        // 违法违规
        "贩毒", "走私", "黑市", "假币", "诈骗"
    ));

    public ContentModerationService(ContentModerationProperties properties,
                                   ContentModerationLogRepository logRepository) {
        this.properties = properties;
        this.logRepository = logRepository;
    }

    /**
     * 审核内容
     * @param userId 用户ID
     * @param contentType 内容类型：ai_input, ai_output, message, contact
     * @param content 待审核内容
     * @return 审核结果
     */
    @Transactional
    public ModerationResult moderateContent(Long userId, String contentType, String content) {
        if (!properties.isEnabled()) {
            log.debug("Content moderation is disabled, passing content");
            return ModerationResult.pass("disabled");
        }

        if (content == null || content.trim().isEmpty()) {
            return ModerationResult.pass("local");
        }

        ModerationResult result;

        try {
            // 根据配置选择审核服务商
            switch (properties.getProvider().toLowerCase()) {
                case "aliyun":
                    result = moderateWithAliyun(content);
                    break;
                case "tencent":
                    result = moderateWithTencent(content);
                    break;
                case "local":
                default:
                    result = moderateWithLocalDictionary(content);
                    break;
            }
        } catch (Exception e) {
            log.error("Content moderation failed, falling back to local dictionary", e);
            result = moderateWithLocalDictionary(content);
        }

        // 记录审核日志
        saveLog(userId, contentType, content, result);

        return result;
    }

    /**
     * 使用本地敏感词库审核（降级方案）
     */
    private ModerationResult moderateWithLocalDictionary(String content) {
        String lowerContent = content.toLowerCase();
        
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (lowerContent.contains(sensitiveWord)) {
                log.warn("Sensitive word detected: {}", sensitiveWord);
                return ModerationResult.reject("high", 
                    "检测到敏感词: " + sensitiveWord, "local");
            }
        }

        return ModerationResult.pass("local");
    }

    /**
     * 使用阿里云内容安全审核
     * TODO: 集成阿里云内容安全API
     */
    private ModerationResult moderateWithAliyun(String content) {
        log.info("Using Aliyun content moderation (not implemented yet, falling back to local)");
        // 实际实现需要调用阿里云API
        // 这里先使用本地词库
        return moderateWithLocalDictionary(content);
    }

    /**
     * 使用腾讯云天御审核
     * TODO: 集成腾讯云天御API
     */
    private ModerationResult moderateWithTencent(String content) {
        log.info("Using Tencent content moderation (not implemented yet, falling back to local)");
        // 实际实现需要调用腾讯云API
        // 这里先使用本地词库
        return moderateWithLocalDictionary(content);
    }

    /**
     * 过滤敏感词（将敏感词替换为***）
     */
    public String filterSensitiveWords(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String filtered = content;
        for (String sensitiveWord : SENSITIVE_WORDS) {
            if (filtered.contains(sensitiveWord)) {
                filtered = filtered.replace(sensitiveWord, "***");
            }
        }
        return filtered;
    }

    /**
     * 保存审核日志
     */
    private void saveLog(Long userId, String contentType, String content, ModerationResult result) {
        try {
            ContentModerationLog logEntry = new ContentModerationLog();
            logEntry.setUserId(userId);
            logEntry.setContentType(contentType);
            // 只保存前500个字符以节省空间
            logEntry.setContent(content.length() > 500 ? content.substring(0, 500) + "..." : content);
            logEntry.setModerationResult(result.getResult());
            logEntry.setRiskLevel(result.getRiskLevel());
            logEntry.setRiskDetails(result.getRiskDetails());
            logEntry.setProvider(result.getProvider());

            logRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save moderation log", e);
            // 不抛出异常，避免影响主流程
        }
    }

    /**
     * 检查内容是否安全
     */
    public boolean isContentSafe(String content) {
        ModerationResult result = moderateContent(null, "check", content);
        return result.isPassed();
    }
}

