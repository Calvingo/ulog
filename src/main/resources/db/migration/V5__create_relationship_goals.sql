-- 关系目标表
CREATE TABLE IF NOT EXISTS relationship_goals (
    goal_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    contact_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    goal_description TEXT NOT NULL,
    ai_strategy TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT fk_goals_contact FOREIGN KEY (contact_id) REFERENCES contacts (cid),
    CONSTRAINT fk_goals_user FOREIGN KEY (user_id) REFERENCES users (uid)
) ENGINE = InnoDB;

CREATE INDEX idx_goals_user ON relationship_goals (user_id);
CREATE INDEX idx_goals_contact ON relationship_goals (contact_id);
CREATE INDEX idx_goals_status ON relationship_goals (status);

-- 行动计划表
CREATE TABLE IF NOT EXISTS action_plans (
    plan_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    scheduled_time DATETIME NOT NULL,
    is_adopted TINYINT NOT NULL DEFAULT 1,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    completed_at DATETIME,
    order_index INT NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT DEFAULT 0,
    CONSTRAINT fk_action_plans_goal FOREIGN KEY (goal_id) REFERENCES relationship_goals (goal_id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_action_plans_goal ON action_plans (goal_id);
CREATE INDEX idx_action_plans_status ON action_plans (status);
CREATE INDEX idx_action_plans_scheduled ON action_plans (scheduled_time);

-- 提醒表
CREATE TABLE IF NOT EXISTS reminders (
    reminder_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    remind_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    sent_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_reminders_plan FOREIGN KEY (plan_id) REFERENCES action_plans (plan_id) ON DELETE CASCADE,
    CONSTRAINT fk_reminders_user FOREIGN KEY (user_id) REFERENCES users (uid)
) ENGINE = InnoDB;

CREATE INDEX idx_reminders_plan ON reminders (plan_id);
CREATE INDEX idx_reminders_user ON reminders (user_id);
CREATE INDEX idx_reminders_time_status ON reminders (remind_time, status);

-- 用户推送令牌表
CREATE TABLE IF NOT EXISTS user_push_tokens (
    token_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    device_token VARCHAR(512) NOT NULL,
    device_type VARCHAR(20) NOT NULL,
    is_active TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_push_tokens_user FOREIGN KEY (user_id) REFERENCES users (uid),
    UNIQUE KEY uk_device_token (device_token)
) ENGINE = InnoDB;

CREATE INDEX idx_push_tokens_user ON user_push_tokens (user_id);
CREATE INDEX idx_push_tokens_active ON user_push_tokens (is_active);

