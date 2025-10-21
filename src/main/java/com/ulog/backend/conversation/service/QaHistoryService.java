package com.ulog.backend.conversation.service;

import com.ulog.backend.conversation.dto.QaHistoryEntry;

import java.util.List;

/**
 * QA对话历史服务接口
 */
public interface QaHistoryService {
    
    /**
     * 添加QA对话条目到联系人会话
     * @param sessionId 会话ID
     * @param entry QA对话条目
     */
    void addContactQaEntry(String sessionId, QaHistoryEntry entry);
    
    /**
     * 添加QA对话条目到用户会话
     * @param sessionId 会话ID
     * @param entry QA对话条目
     */
    void addUserQaEntry(String sessionId, QaHistoryEntry entry);
    
    /**
     * 更新联系人会话的最后一条QA历史记录
     * @param sessionId 会话ID
     * @param entry 更新后的QA对话条目
     */
    void updateLastContactQaEntry(String sessionId, QaHistoryEntry entry);
    
    /**
     * 更新用户会话的最后一条QA历史记录
     * @param sessionId 会话ID
     * @param entry 更新后的QA对话条目
     */
    void updateLastUserQaEntry(String sessionId, QaHistoryEntry entry);
    
    /**
     * 获取联系人会话的QA历史
     * @param sessionId 会话ID
     * @return QA历史列表
     */
    List<QaHistoryEntry> getContactQaHistory(String sessionId);
    
    /**
     * 获取用户会话的QA历史
     * @param sessionId 会话ID
     * @return QA历史列表
     */
    List<QaHistoryEntry> getUserQaHistory(String sessionId);
    
    /**
     * 格式化QA历史为Prompt可读格式
     * @param history QA历史列表
     * @return 格式化后的字符串
     */
    String formatQaHistoryForPrompt(List<QaHistoryEntry> history);
}
