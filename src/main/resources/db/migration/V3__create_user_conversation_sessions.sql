-- 用户自我信息收集会话表
CREATE TABLE user_conversation_sessions (
    session_id VARCHAR(100) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    current_dimension VARCHAR(100),
    completed_dimensions TEXT,
    collected_data TEXT,
    conversation_history TEXT,
    final_description TEXT,
    last_question TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    INDEX idx_ucs_user_status (user_id, status),
    INDEX idx_ucs_user_id (user_id),
    INDEX idx_ucs_created_at (created_at),
    
         FOREIGN KEY (user_id) REFERENCES users(uid) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

