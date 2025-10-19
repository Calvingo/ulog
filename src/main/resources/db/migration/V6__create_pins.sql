-- Pin收藏表
CREATE TABLE IF NOT EXISTS pins (
    pin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    
    -- 来源信息
    source_type VARCHAR(20) NOT NULL COMMENT '来源类型：CONTACT_QA/USER_QA',
    session_id VARCHAR(100) NOT NULL COMMENT '来源会话ID',
    qa_index INT NOT NULL COMMENT 'QA在历史中的索引（第几个QA，从0开始）',
    contact_id BIGINT NULL COMMENT '关联联系人ID（仅联系人QA有）',
    
    -- Pin内容（完整保存QaHistoryEntry结构）
    question TEXT NOT NULL COMMENT '用户的问题',
    answer TEXT NOT NULL COMMENT 'AI的直接回答',
    supplement_question TEXT COMMENT 'AI的补充问题（如果需要补充信息）',
    supplement_answer TEXT COMMENT '补充信息后的最终回答',
    needs_more_info TINYINT DEFAULT 0 COMMENT '是否需要补充信息',
    
    -- 上下文信息（JSON格式：联系人名称、会话类型等）
    context_info TEXT COMMENT '上下文元数据',
    
    -- 用户标注
    note VARCHAR(500) COMMENT '用户备注',
    tags VARCHAR(255) COMMENT '标签（逗号分隔）',
    
    -- 时间戳
    qa_timestamp VARCHAR(50) COMMENT 'QA发生的时间（从QaHistoryEntry.timestamp复制）',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Pin创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- 外键和索引
    CONSTRAINT fk_pins_user FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE CASCADE,
    CONSTRAINT fk_pins_contact FOREIGN KEY (contact_id) REFERENCES contacts(cid) ON DELETE SET NULL,
    
    -- 防止重复Pin同一个QA
    UNIQUE KEY uk_user_session_qa (user_id, session_id, qa_index),
    
    INDEX idx_pins_user (user_id),
    INDEX idx_pins_source_type (source_type),
    INDEX idx_pins_contact (contact_id),
    INDEX idx_pins_created (created_at),
    INDEX idx_pins_user_contact (user_id, contact_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Pin收藏表';

