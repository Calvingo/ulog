-- V8: 调整举报表结构，适配AI助手反馈场景
-- 将举报功能从"用户举报"改为"AI内容反馈/系统问题报告"

-- 步骤1: 重命名列（如果还未重命名）
-- 检查列是否存在，如果存在则重命名
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND COLUMN_NAME = 'report_category');
SET @sql_stmt = IF(@col_exists > 0, 
    'ALTER TABLE user_report CHANGE COLUMN report_category target_type VARCHAR(100)', 
    'SELECT "report_category already renamed" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND COLUMN_NAME = 'content');
SET @sql_stmt = IF(@col_exists > 0, 
    'ALTER TABLE user_report CHANGE COLUMN content description TEXT NOT NULL', 
    'SELECT "content already renamed" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND COLUMN_NAME = 'evidence');
SET @sql_stmt = IF(@col_exists > 0, 
    'ALTER TABLE user_report CHANGE COLUMN evidence context TEXT', 
    'SELECT "evidence already renamed" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 步骤2: 修改状态字段默认值
ALTER TABLE user_report ALTER COLUMN status SET DEFAULT 'PENDING';

-- 步骤3: 添加新列（如果不存在）
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND COLUMN_NAME = 'target_id');
SET @sql_stmt = IF(@col_exists = 0, 
    'ALTER TABLE user_report ADD COLUMN target_id VARCHAR(255)', 
    'SELECT "target_id already exists" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND COLUMN_NAME = 'evidence_urls');
SET @sql_stmt = IF(@col_exists = 0, 
    'ALTER TABLE user_report ADD COLUMN evidence_urls TEXT', 
    'SELECT "evidence_urls already exists" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 步骤4: 添加新的索引（如果不存在）
SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND INDEX_NAME = 'idx_ur_target_type');
SET @sql_stmt = IF(@index_exists = 0, 
    'CREATE INDEX idx_ur_target_type ON user_report(target_type)', 
    'SELECT "idx_ur_target_type already exists" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @index_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS 
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user_report' AND INDEX_NAME = 'idx_ur_target_id');
SET @sql_stmt = IF(@index_exists = 0, 
    'CREATE INDEX idx_ur_target_id ON user_report(target_id)', 
    'SELECT "idx_ur_target_id already exists" AS msg');
PREPARE stmt FROM @sql_stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

