-- V8: 调整举报表结构，适配AI助手反馈场景
-- 将举报功能从"用户举报"改为"AI内容反馈/系统问题报告"

-- 修改表结构
ALTER TABLE user_report 
    -- 移除被举报用户ID（因为不再举报用户）
    DROP COLUMN reported_user_id,
    
    -- 修改举报类型字段说明
    MODIFY COLUMN report_type VARCHAR(50) NOT NULL 
        COMMENT 'AI_INAPPROPRIATE_CONTENT, AI_POOR_QUALITY, SYSTEM_BUG, OTHER',
    
    -- 将report_category改为target_type（举报目标类型）
    CHANGE COLUMN report_category target_type VARCHAR(50) 
        COMMENT 'QA_RESPONSE, USER_QA_RESPONSE, AI_SUMMARY, GOAL_STRATEGY, SYSTEM_FEATURE, OTHER',
    
    -- 添加目标ID字段（用于定位具体的QA、总结等）
    ADD COLUMN target_id VARCHAR(255) 
        COMMENT '目标ID，如sessionId、qaIndex、goalId等' AFTER target_type,
    
    -- 将content改为description（更明确）
    CHANGE COLUMN content description TEXT NOT NULL 
        COMMENT '问题描述',
    
    -- 将evidence改为context（上下文信息，JSON格式）
    CHANGE COLUMN evidence context TEXT 
        COMMENT '上下文信息（JSON格式），包含问题、答案、时间戳等',
    
    -- 添加证据URL字段
    ADD COLUMN evidence_urls TEXT 
        COMMENT '证据截图URLs（JSON数组）' AFTER context,
    
    -- 修改状态字段说明
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING' 
        COMMENT 'PENDING, REVIEWING, RESOLVED, DISMISSED';

-- 添加新的索引
ALTER TABLE user_report
    DROP INDEX idx_reported_user_id,  -- 删除旧索引
    ADD INDEX idx_target_type (target_type),
    ADD INDEX idx_target_id (target_id),
    ADD INDEX idx_status (status),
    ADD INDEX idx_created_at (created_at);

-- 更新表注释
ALTER TABLE user_report 
    COMMENT = 'AI内容反馈和系统问题报告表';

