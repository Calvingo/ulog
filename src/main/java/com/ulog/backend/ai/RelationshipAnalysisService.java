package com.ulog.backend.ai;

import reactor.core.publisher.Mono;

/**
 * 关系分析服务接口
 * 提供关系分析和交往建议生成功能
 */
public interface RelationshipAnalysisService {

    /**
     * 生成关系分析
     * @param contactId 联系人ID
     * @param userId 用户ID
     * @return 关系分析文本
     */
    Mono<String> generateRelationshipAnalysis(Long contactId, Long userId);

    /**
     * 生成交往建议
     * @param contactId 联系人ID
     * @return 交往建议文本
     */
    Mono<String> generateInteractionSuggestions(Long contactId);
}
