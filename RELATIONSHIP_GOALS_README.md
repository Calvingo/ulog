# 关系目标功能文档

## 功能概述

关系目标功能允许用户为特定联系人设定关系改善目标，系统会通过AI（Deepseek）自动生成策略和具体的行动计划，并通过推送通知定时提醒用户执行行动计划。

## 主要特性

1. **AI策略生成**：根据联系人信息和用户输入的目标，自动生成关系改善策略
2. **行动计划**：生成3-5个具体的行动计划，每个计划包含标题、描述和执行时间
3. **采纳管理**：用户可以选择是否采纳AI生成的行动建议
4. **推送提醒**：为已采纳的行动计划创建定时提醒，通过FCM推送到用户设备
5. **状态跟踪**：支持标记行动计划的完成、跳过等状态

## 数据库结构

### 新增表

1. **relationship_goals** - 关系目标表
   - 存储用户为联系人设定的目标
   - 包含AI生成的策略

2. **action_plans** - 行动计划表
   - 存储具体的行动步骤
   - `is_adopted` 字段标记用户是否采纳该计划

3. **reminders** - 提醒表
   - 存储所有待发送的提醒
   - 只为已采纳的行动计划创建

4. **user_push_tokens** - 推送令牌表
   - 存储用户设备的FCM推送令牌

## API接口

### 关系目标管理

#### 1. 创建关系目标
```http
POST /api/goals
Authorization: Bearer {token}
Content-Type: application/json

{
  "contactId": 1,
  "goalDescription": "希望能够和这个联系人建立更深厚的友谊"
}
```

响应：
- 返回目标详情，包含AI生成的策略和行动计划
- 自动为已采纳的行动计划创建提醒

#### 2. 获取目标列表
```http
GET /api/goals?contactId=1
Authorization: Bearer {token}
```

#### 3. 获取目标详情
```http
GET /api/goals/{goalId}
Authorization: Bearer {token}
```

#### 4. 更新目标
```http
PUT /api/goals/{goalId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "goalDescription": "更新后的目标描述",
  "status": "COMPLETED"
}
```

#### 5. 删除目标
```http
DELETE /api/goals/{goalId}
Authorization: Bearer {token}
```

### 行动计划管理

#### 6. 更新行动计划状态
```http
PUT /api/goals/{goalId}/action-plans/{planId}/status
Authorization: Bearer {token}
Content-Type: application/json

{
  "status": "COMPLETED",
  "completedAt": "2025-10-20T10:00:00"
}
```

可用状态：
- `PENDING` - 待执行
- `COMPLETED` - 已完成
- `SKIPPED` - 已跳过
- `CANCELLED` - 已取消

#### 7. 更新行动计划采纳状态
```http
PUT /api/goals/{goalId}/action-plans/{planId}/adoption
Authorization: Bearer {token}
Content-Type: application/json

{
  "isAdopted": true
}
```

**重要**：
- 设置为 `true` 时，系统会自动创建提醒
- 设置为 `false` 时，系统会取消相关的未发送提醒

#### 8. 重新生成策略
```http
POST /api/goals/{goalId}/regenerate
Authorization: Bearer {token}
```

### 提醒管理

#### 9. 获取即将到来的提醒
```http
GET /api/goals/reminders/upcoming
Authorization: Bearer {token}
```

### 推送令牌管理

#### 10. 注册推送令牌
```http
POST /api/push/tokens
Authorization: Bearer {token}
Content-Type: application/json

{
  "deviceToken": "fcm_device_token_here",
  "deviceType": "ANDROID"
}
```

设备类型：
- `ANDROID` - 安卓设备
- `IOS` - iOS设备

#### 11. 注销推送令牌
```http
DELETE /api/push/tokens/{tokenId}
Authorization: Bearer {token}
```

## 配置说明

### application.properties

```properties
# 调度器配置
spring.task.scheduling.enabled=true
reminder.scheduler.cron=0 * * * * *  # 每分钟检查一次

# 提醒提前时间（分钟）
reminder.advance-minutes=15

# Firebase配置文件路径
firebase.config-path=${FIREBASE_CONFIG_PATH:classpath:firebase-service-account.json}
```

### Firebase配置

1. 从Firebase控制台下载服务账号密钥（JSON文件）
2. 将文件放置在 `src/main/resources/` 目录下，命名为 `firebase-service-account.json`
3. 或者通过环境变量 `FIREBASE_CONFIG_PATH` 指定文件路径

**注意**：如果未配置Firebase，推送功能将被禁用，但其他功能正常工作。

## 工作流程

### 1. 创建目标流程

```mermaid
用户创建目标
    ↓
调用AI生成策略和行动计划
    ↓
保存目标和行动计划到数据库
    ↓
为已采纳的行动计划创建提醒
    ↓
返回完整的目标详情
```

### 2. 提醒发送流程

```mermaid
定时任务每分钟执行
    ↓
查询到期的PENDING状态提醒
    ↓
获取用户的活跃推送令牌
    ↓
发送推送通知
    ↓
更新提醒状态为SENT或FAILED
```

### 3. 采纳状态更新流程

```mermaid
用户更新is_adopted状态
    ↓
检查之前的状态
    ↓
如果从false变为true：创建提醒
如果从true变为false：取消未发送的提醒
    ↓
保存更新
```

## AI Prompt设计

系统使用精心设计的Prompt要求Deepseek返回结构化的JSON响应：

```json
{
  "strategy": "整体策略说明（200-300字）",
  "actionPlans": [
    {
      "title": "行动标题",
      "description": "详细描述",
      "scheduledDays": 0
    }
  ]
}
```

其中 `scheduledDays` 表示距今多少天后执行（0表示立即，7表示一周后）。

## 定时任务

### ReminderSchedulerService

- 使用 `@Scheduled` 注解，默认每分钟执行一次
- 查询所有到期且状态为PENDING的提醒
- 调用推送服务发送通知
- 更新提醒状态

### 配置调整

可以通过 `reminder.scheduler.cron` 配置项调整执行频率：
- `0 * * * * *` - 每分钟
- `0 */5 * * * *` - 每5分钟
- `0 0 * * * *` - 每小时

## 测试

### 运行集成测试

```bash
mvn test -Dtest=RelationshipGoalIntegrationTest
```

### 测试覆盖

- 创建目标
- 更新目标
- 删除目标
- 列出目标
- 按联系人筛选目标

## 注意事项

1. **AI生成失败处理**：如果AI生成失败，目标仍会被创建，但不会有策略和行动计划
2. **推送失败处理**：推送失败的提醒会被标记为FAILED状态，可以实现重试机制
3. **时区处理**：系统使用UTC时间存储，前端需要按用户时区显示
4. **事务管理**：创建目标的过程在同一事务中完成，确保数据一致性
5. **软删除**：目标和行动计划都使用软删除，不会真正从数据库中移除

## 扩展建议

1. **分布式部署**：如果部署多个实例，建议为定时任务添加分布式锁（如Redis）
2. **推送重试**：可以实现失败推送的重试机制
3. **通知渠道**：可以扩展支持邮件、短信等其他通知方式
4. **行动计划编辑**：目前行动计划由AI生成，可以扩展支持用户手动编辑
5. **统计分析**：可以添加完成率、目标达成情况等统计功能

## 依赖项

### Maven依赖

```xml
<dependency>
    <groupId>com.google.firebase</groupId>
    <artifactId>firebase-admin</artifactId>
    <version>9.2.0</version>
</dependency>
```

## 数据库迁移

执行数据库迁移：
```bash
mvn flyway:migrate
```

或启动应用时自动执行（已配置 `spring.flyway.enabled=true`）。

## 故障排除

### 1. Firebase初始化失败
- 检查配置文件路径是否正确
- 确认配置文件格式是否有效
- 查看日志中的详细错误信息

### 2. AI生成失败
- 检查Deepseek API密钥是否有效
- 确认网络连接正常
- 查看AI服务的响应内容

### 3. 推送通知未收到
- 确认设备令牌已正确注册
- 检查Firebase项目配置
- 验证设备是否允许推送通知

### 4. 提醒未按时发送
- 检查定时任务是否正常运行
- 确认 `spring.task.scheduling.enabled=true`
- 查看调度器日志

## 开发团队

如有问题，请联系开发团队。

