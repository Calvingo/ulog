package com.ulog.backend.conversation.service;

import com.ulog.backend.conversation.dto.SelfValue;
import java.util.Map;

/**
 * Self Value 计算服务接口
 * 负责基于对话数据计算用户的自我价值评分
 */
public interface SelfValueCalculationService {

    /**
     * 同步计算 Self Value（用于必须立即获取的场景）
     * @param collectedData 收集到的对话数据
     * @return SelfValue 对象
     */
    SelfValue calculateSelfValue(Map<String, Object> collectedData);

    /**
     * 异步计算并更新联系人的 Self Value
     * @param contactId 联系人ID
     * @param collectedData 收集到的对话数据
     */
    void calculateAndUpdateContactAsync(Long contactId, Map<String, Object> collectedData);

    /**
     * 异步计算并更新用户的 Self Value
     * @param userId 用户ID
     * @param collectedData 收集到的对话数据
     */
    void calculateAndUpdateUserAsync(Long userId, Map<String, Object> collectedData);
}
