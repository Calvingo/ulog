-- 隐私协议同意记录表
CREATE TABLE user_privacy_consent (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    policy_version VARCHAR(20) NOT NULL,
    consent_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent VARCHAR(512),
    INDEX idx_upc_user_id (user_id),
    INDEX idx_upc_consent_time (consent_time),
    FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 内容审核记录表
CREATE TABLE content_moderation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    content_type VARCHAR(50) NOT NULL COMMENT 'message, contact, ai_input, ai_output',
    content TEXT,
    moderation_result VARCHAR(20) NOT NULL COMMENT 'pass, reject, review',
    risk_level VARCHAR(20) COMMENT 'low, medium, high',
    risk_details TEXT COMMENT '风险详情（JSON格式）',
    provider VARCHAR(50) COMMENT '审核服务商：aliyun, tencent, local',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_cml_user_id (user_id),
    INDEX idx_cml_created_at (created_at),
    INDEX idx_cml_moderation_result (moderation_result),
    FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 用户举报表
CREATE TABLE user_report (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reporter_id BIGINT NOT NULL COMMENT '举报人ID',
    reported_user_id BIGINT COMMENT '被举报用户ID（可选）',
    report_type VARCHAR(50) NOT NULL COMMENT 'inappropriate_content, violation, harassment, spam, other',
    report_category VARCHAR(100) COMMENT '举报分类',
    content TEXT NOT NULL COMMENT '举报内容描述',
    evidence TEXT COMMENT '证据（JSON格式，可包含截图URL等）',
    status VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT 'pending, processing, resolved, rejected',
    admin_notes TEXT COMMENT '管理员备注',
    processed_by BIGINT COMMENT '处理管理员ID',
    processed_at TIMESTAMP NULL COMMENT '处理时间',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_ur_reporter_id (reporter_id),
    INDEX idx_ur_reported_user_id (reported_user_id),
    INDEX idx_ur_status (status),
    INDEX idx_ur_created_at (created_at),
    CONSTRAINT fk_ur_reporter FOREIGN KEY (reporter_id) REFERENCES users(uid) ON DELETE CASCADE,
    CONSTRAINT fk_ur_reported_user FOREIGN KEY (reported_user_id) REFERENCES users(uid) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 操作日志表
CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    operation_type VARCHAR(50) NOT NULL COMMENT 'login, register, password_change, account_delete, ai_chat, etc',
    operation_detail TEXT COMMENT '操作详情（JSON格式）',
    ip_address VARCHAR(45),
    user_agent TEXT,
    request_uri VARCHAR(512),
    http_method VARCHAR(10),
    status_code INT COMMENT 'HTTP状态码',
    error_message TEXT COMMENT '错误信息（如有）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ol_user_id (user_id),
    INDEX idx_ol_operation_type (operation_type),
    INDEX idx_ol_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

