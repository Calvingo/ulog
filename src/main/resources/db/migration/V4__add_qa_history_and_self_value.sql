-- 添加QA历史和自我价值评分字段

-- 联系人会话表：添加QA历史字段
ALTER TABLE conversation_sessions ADD COLUMN qa_history TEXT COMMENT 'QA对话历史，JSON格式，永久保存';

-- 用户会话表：添加QA历史字段
ALTER TABLE user_conversation_sessions ADD COLUMN qa_history TEXT COMMENT 'QA对话历史，JSON格式，永久保存';

-- 用户表：添加自我价值评分字段
ALTER TABLE users ADD COLUMN self_value VARCHAR(50) COMMENT '自我价值评分，格式：4.5,3.5,2.5,4.0,3.25，仅该用户可见';

-- 联系人表：添加自我价值评分字段
ALTER TABLE contacts ADD COLUMN self_value VARCHAR(50) COMMENT '自我价值评分，格式：4.5,3.5,2.5,4.0,3.25，仅该用户可见';

-- 添加索引以提高查询性能
CREATE INDEX idx_users_self_value ON users(self_value);
CREATE INDEX idx_contacts_self_value ON contacts(self_value);
