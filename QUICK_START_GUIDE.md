# 关系目标功能快速开始指南

## 前置条件

1. Java 17+
2. MySQL 数据库
3. Maven
4. Firebase账号（用于推送通知）

## 第一步：数据库迁移

应用启动时会自动执行Flyway迁移，创建所需的表结构。

或者手动执行：
```bash
mvn flyway:migrate
```

## 第二步：配置Firebase（可选）

### 2.1 获取Firebase服务账号密钥

1. 访问 [Firebase Console](https://console.firebase.google.com/)
2. 选择或创建项目
3. 进入 **项目设置** > **服务账号**
4. 点击 **生成新的私钥**
5. 下载JSON文件

### 2.2 配置文件

将下载的JSON文件放置到 `src/main/resources/` 目录，命名为 `firebase-service-account.json`

或通过环境变量指定：
```bash
export FIREBASE_CONFIG_PATH=/path/to/firebase-service-account.json
```

**注意**：如果不配置Firebase，推送功能将被禁用，但其他功能正常工作。

## 第三步：编译并运行

```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run
```

## 第四步：测试API

### 4.1 获取访问令牌

首先需要登录获取JWT令牌：

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "+8613800138000",
    "password": "your_password"
  }'
```

保存返回的 `accessToken`。

### 4.2 创建联系人

如果还没有联系人，先创建一个：

```bash
curl -X POST http://localhost:8080/api/contacts \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "name": "张三",
    "description": "大学同学，在北京工作，喜欢运动"
  }'
```

### 4.3 创建关系目标

```bash
curl -X POST http://localhost:8080/api/goals \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "contactId": 1,
    "goalDescription": "希望能够和张三恢复联系，重建友谊"
  }'
```

系统会：
1. 调用AI生成策略
2. 创建3-5个行动计划
3. 自动为已采纳的计划创建提醒

### 4.4 查看目标详情

```bash
curl -X GET http://localhost:8080/api/goals/1 \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

响应示例：
```json
{
  "code": 0,
  "message": "ok",
  "data": {
    "goalId": 1,
    "contactId": 1,
    "contactName": "张三",
    "goalDescription": "希望能够和张三恢复联系，重建友谊",
    "aiStrategy": "为了重建与张三的友谊，建议采取循序渐进的方式...",
    "status": "ACTIVE",
    "actionPlans": [
      {
        "planId": 1,
        "title": "发送问候消息",
        "description": "通过微信发送一条关心的消息，询问对方近况",
        "scheduledTime": "2025-10-18T16:00:00",
        "isAdopted": true,
        "status": "PENDING",
        "orderIndex": 0
      },
      {
        "planId": 2,
        "title": "约饭聊天",
        "description": "邀请张三共进午餐或晚餐，面对面交流",
        "scheduledTime": "2025-10-25T12:00:00",
        "isAdopted": true,
        "status": "PENDING",
        "orderIndex": 1
      }
    ],
    "createdAt": "2025-10-18T15:30:00",
    "updatedAt": "2025-10-18T15:30:00"
  }
}
```

### 4.5 注册推送令牌（移动端）

移动端获取FCM token后注册：

```bash
curl -X POST http://localhost:8080/api/push/tokens \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "deviceToken": "your_fcm_device_token",
    "deviceType": "ANDROID"
  }'
```

### 4.6 管理行动计划

#### 取消采纳某个行动计划：

```bash
curl -X PUT http://localhost:8080/api/goals/1/action-plans/2/adoption \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "isAdopted": false
  }'
```

系统会自动取消该计划相关的未发送提醒。

#### 标记行动计划为已完成：

```bash
curl -X PUT http://localhost:8080/api/goals/1/action-plans/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN" \
  -d '{
    "status": "COMPLETED"
  }'
```

### 4.7 查看即将到来的提醒

```bash
curl -X GET http://localhost:8080/api/goals/reminders/upcoming \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 第五步：观察定时任务

定时任务每分钟会检查一次待发送的提醒。查看日志：

```
2025-10-18 16:00:00 INFO  ReminderSchedulerService - Found 1 pending reminders to send
2025-10-18 16:00:01 INFO  ReminderSchedulerService - Successfully sent reminder 1
```

## 使用Postman测试

导入 `relationship_goals_api_examples.json` 到Postman：

1. 打开Postman
2. 点击 **Import**
3. 选择 `relationship_goals_api_examples.json` 文件
4. 设置环境变量：
   - `base_url`: http://localhost:8080
   - `access_token`: 你的JWT令牌

## 常见问题

### Q1: AI生成失败怎么办？

检查：
- Deepseek API密钥是否有效
- 网络连接是否正常
- 查看日志中的详细错误信息

目标仍会被创建，可以稍后使用"重新生成策略"功能。

### Q2: 推送通知未收到

检查：
1. Firebase配置是否正确
2. 设备令牌是否已注册
3. 提醒时间是否已到
4. 行动计划是否已采纳
5. 查看调度器日志

### Q3: 如何自定义提醒时间？

修改配置：
```properties
# 提前15分钟提醒
reminder.advance-minutes=15
```

### Q4: 如何调整定时任务频率？

修改配置：
```properties
# 每5分钟执行一次
reminder.scheduler.cron=0 */5 * * * *
```

## 数据流程图

```
用户创建目标
    ↓
AI生成策略和行动计划
    ↓
保存到数据库
    ↓
为已采纳的计划创建提醒（scheduledTime - 15分钟）
    ↓
定时任务每分钟检查
    ↓
到期提醒 → 发送推送通知 → 更新状态为SENT
```

## 下一步

- 阅读完整的 [RELATIONSHIP_GOALS_README.md](./RELATIONSHIP_GOALS_README.md) 了解详细功能
- 查看 Swagger UI: http://localhost:8080/swagger-ui
- 探索更多API功能

## 技术支持

如有问题，请查看：
1. 应用日志
2. Swagger API文档
3. README文档

祝使用愉快！🎉

