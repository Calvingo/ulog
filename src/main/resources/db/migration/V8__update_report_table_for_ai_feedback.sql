-- V8: 调整举报表结构，适配AI助手反馈场景
-- 将举报功能从"用户举报"改为"AI内容反馈/系统问题报告"

-- 步骤1: 删除外键约束
ALTER TABLE user_report DROP CONSTRAINT fk_ur_reported_user;

-- 步骤2: 删除旧索引
ALTER TABLE user_report DROP INDEX idx_ur_reported_user_id;

-- 步骤3: 删除不再需要的列
ALTER TABLE user_report DROP COLUMN reported_user_id;

-- 步骤4: 重命名列（H2 兼容方式）
ALTER TABLE user_report ALTER COLUMN report_category RENAME TO target_type;
ALTER TABLE user_report ALTER COLUMN content RENAME TO description;
ALTER TABLE user_report ALTER COLUMN evidence RENAME TO context;

-- 步骤5: 修改列类型（确保与新需求匹配）
ALTER TABLE user_report ALTER COLUMN target_type VARCHAR(100);
ALTER TABLE user_report ALTER COLUMN status SET DEFAULT 'PENDING';

-- 步骤6: 添加新列
ALTER TABLE user_report ADD COLUMN target_id VARCHAR(255);
ALTER TABLE user_report ADD COLUMN evidence_urls TEXT;

-- 步骤7: 添加新的索引
CREATE INDEX idx_ur_target_type ON user_report(target_type);
CREATE INDEX idx_ur_target_id ON user_report(target_id);

