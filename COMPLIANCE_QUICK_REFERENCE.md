# 合规功能快速参考指南

## 🎯 开发者必读

### 添加操作日志

在Controller方法上添加注解：

```java
@PostMapping("/api/some-action")
@LogOperation(value = "action_type", description = "操作描述")
public ApiResponse<ResponseDto> someAction() {
    // 方法会自动记录日志
}
```

### 内容审核

在需要审核内容的地方调用：

```java
@Autowired
private ContentModerationService contentModerationService;

public void handleUserContent(String content, Long userId) {
    ModerationResult result = contentModerationService.moderateContent(
        userId, "content_type", content);
    
    if (!result.isPassed()) {
        throw new BadRequestException("内容包含敏感信息");
    }
}
```

### 检查隐私协议同意

```java
@Autowired
private PrivacyConsentService privacyConsentService;

public void requirePrivacyConsent(Long userId) {
    if (!privacyConsentService.hasConsentedToCurrentPolicy(userId)) {
        throw new ApiException(ErrorCode.BAD_REQUEST, "请先同意隐私政策");
    }
}
```

## 📊 API端点总览

### 隐私协议
- `GET /api/privacy/policy` - 获取隐私政策
- `GET /api/privacy/consent/status` - 检查同意状态
- `POST /api/privacy/consent` - 记录同意

### 举报系统
- `POST /api/reports` - 提交举报
- `GET /api/reports/my-reports` - 我的举报
- `GET /api/reports/pending` - 待处理举报（管理员）
- `GET /api/reports/all` - 所有举报（管理员）

## 🔧 常用配置

### 启用/禁用功能

```properties
# 禁用内容审核（仅开发环境）
content.moderation.enabled=false

# 禁用限流（仅开发环境）
rate.limit.enabled=false

# 禁用隐私政策强制检查（仅开发环境）
privacy.policy.required=false
```

### 调整限流

```properties
# 更严格的限流
rate.limit.login-per-minute=3
rate.limit.ai-per-minute=30

# 更宽松的限流
rate.limit.login-per-minute=10
rate.limit.ai-per-minute=100
```

## 🐛 常见问题

### Q: 内容审核太严格，误杀正常内容？
A: 编辑 `ContentModerationService.SENSITIVE_WORDS` 调整敏感词列表

### Q: 限流导致开发测试不便？
A: 开发环境设置 `rate.limit.enabled=false`

### Q: 如何添加新的操作日志类型？
A: 在Controller方法上添加 `@LogOperation` 注解即可

### Q: 如何查看审核日志？
A: 查询 `content_moderation_log` 表或使用日志服务

## 📝 数据库查询示例

```sql
-- 查看最近的操作日志
SELECT * FROM operation_log 
ORDER BY created_at DESC LIMIT 50;

-- 查看被拒绝的内容
SELECT * FROM content_moderation_log 
WHERE moderation_result = 'reject' 
ORDER BY created_at DESC;

-- 查看待处理的举报
SELECT * FROM user_report 
WHERE status = 'pending' 
ORDER BY created_at DESC;

-- 查看用户的隐私协议同意记录
SELECT * FROM user_privacy_consent 
WHERE user_id = ? 
ORDER BY consent_time DESC;
```

## 🚨 生产环境检查清单

- [ ] 配置真实的隐私政策URL
- [ ] 集成第三方内容审核API
- [ ] 配置邮件通知服务
- [ ] 确认日志保留期≥180天
- [ ] 测试限流是否正常工作
- [ ] 验证敏感词库是否完整
- [ ] 检查数据库索引是否创建
- [ ] 配置定时任务清理旧日志

## 📞 技术支持

遇到问题请检查：
1. 应用日志 `logs/application.log`
2. 数据库连接状态
3. 配置文件格式是否正确

---
**最后更新**: 2025-10-20

