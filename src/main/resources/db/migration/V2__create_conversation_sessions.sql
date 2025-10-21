-- 创建对话会话表
CREATE TABLE conversation_sessions (
    session_id VARCHAR(64) PRIMARY KEY COMMENT '会话ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    contact_id BIGINT NULL COMMENT '关联的联系人ID（完成后填充）',
    
    -- 联系人信息
    contact_name VARCHAR(128) COMMENT '联系人名称（临时存储）',
    
    -- 收集进度
    current_dimension VARCHAR(50) COMMENT '当前收集维度',
    completed_dimensions TEXT COMMENT '已完成的维度（JSON数组）',
    
    -- 数据存储
    collected_data TEXT COMMENT '收集到的数据（JSON对象）',
    conversation_history TEXT COMMENT '对话历史（JSON数组）',
    final_description TEXT COMMENT '最终生成的描述',
    
    -- 会话状态
    status VARCHAR(20) DEFAULT 'ACTIVE' COMMENT '会话状态：ACTIVE/REQUESTING_MINIMUM/CONFIRMING_END/COMPLETED/QA_ACTIVE/ABANDONED/EXPIRED',
    last_question TEXT COMMENT '上一个问题',
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    last_active_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后活跃时间',
    completed_at TIMESTAMP NULL COMMENT '完成时间',
    
    -- 外键和索引
    FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE CASCADE,
    FOREIGN KEY (contact_id) REFERENCES contacts(cid) ON DELETE SET NULL,
    
    INDEX idx_cs_user_status (user_id, status),
    INDEX idx_cs_contact_id (contact_id),
    INDEX idx_cs_created_at (created_at),
    INDEX idx_cs_last_active (last_active_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

