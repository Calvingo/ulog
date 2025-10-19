# Pin收藏功能文档

## 功能概述

Pin功能允许用户将对话中有价值的AI回答收藏起来，单独存储和管理。用户可以在任何时候回顾这些有用的洞察和建议。

## 核心特性

1. **精确定位** - 通过 `qaIndex` 从QA历史中精确定位要Pin的问答
2. **完整保存** - 保存完整的Q&A内容，包括补充问答环节
3. **防重复** - 同一个QA不能重复Pin
4. **灵活筛选** - 支持按联系人、来源类型筛选
5. **用户标注** - 支持添加备注和标签

## 数据结构

### QaHistoryEntry 结构（来源）
```json
{
  "timestamp": "2025-10-18T16:00:00",
  "question": "张三和我的关系如何？",
  "answer": "根据现有信息，你们是大学同学...",
  "supplementQuestion": "请补充你们的互动细节",  // 可选
  "supplementAnswer": "基于你的补充...",        // 可选
  "needsMoreInfo": false
}
```

### Pin 存储结构
```json
{
  "pinId": 1,
  "sourceType": "CONTACT_QA",
  "sessionId": "session-123",
  "qaIndex": 0,
  "contactId": 5,
  "contactName": "张三",
  "question": "张三和我的关系如何？",
  "answer": "根据现有信息，你们是大学同学...",
  "supplementQuestion": "请补充你们的互动细节",
  "supplementAnswer": "基于你的补充...",
  "hasSupplementInfo": true,
  "note": "这个洞察很有价值",
  "tags": ["关系分析", "重要"],
  "qaTimestamp": "2025-10-18T16:00:00",
  "createdAt": "2025-10-18T16:05:00"
}
```

## API接口

### 1. 创建Pin

```http
POST /api/pins
Authorization: Bearer {token}
Content-Type: application/json

{
  "sessionId": "session-123",
  "qaIndex": 0,
  "note": "这个回答很有价值",
  "tags": "重要,参考"
}
```

**说明：**
- `sessionId`: 会话ID（联系人会话或用户会话）
- `qaIndex`: QA在历史中的索引（从0开始，0表示第1个QA）
- `note`: 用户备注（可选）
- `tags`: 标签，逗号分隔（可选）

**系统自动处理：**
- 自动识别来源类型（CONTACT_QA 或 USER_QA）
- 自动提取联系人信息（如果有）
- 自动复制QA的所有内容（包括补充问答）
- 自动防止重复Pin

### 2. 获取Pin列表

```http
GET /api/pins
Authorization: Bearer {token}
```

**可选参数：**
- `contactId`: 按联系人筛选
- `sourceType`: 按来源类型筛选（CONTACT_QA 或 USER_QA）

**示例：**
```http
GET /api/pins?contactId=5
GET /api/pins?sourceType=CONTACT_QA
GET /api/pins?contactId=5&sourceType=CONTACT_QA
```

### 3. 获取Pin详情

```http
GET /api/pins/{pinId}
Authorization: Bearer {token}
```

返回完整的Pin内容，包括：
- 完整的问题和回答
- 补充问答（如果有）
- 用户备注和标签
- 上下文信息

### 4. 更新Pin

```http
PUT /api/pins/{pinId}
Authorization: Bearer {token}
Content-Type: application/json

{
  "note": "更新后的备注",
  "tags": "新标签1,新标签2"
}
```

**说明：**
- 只能更新备注和标签
- QA内容不可修改（因为来自历史记录）

### 5. 删除Pin

```http
DELETE /api/pins/{pinId}
Authorization: Bearer {token}
```

### 6. 检查是否已Pin

```http
GET /api/pins/check?sessionId={sessionId}&qaIndex={qaIndex}
Authorization: Bearer {token}
```

返回：`true` 或 `false`

## 使用流程

### 典型使用场景

```
1. 用户进行联系人QA对话
   ↓
2. 系统将Q&A保存到 qa_history
   ↓
3. 用户觉得某个回答有价值
   ↓
4. 用户点击"Pin"按钮
   ↓
5. 前端调用 POST /api/pins（传入sessionId和qaIndex）
   ↓
6. 后端从qa_history中提取该QA
   ↓
7. 保存到pins表，添加上下文信息
   ↓
8. 用户可以随时查看所有Pin的内容
```

### qaIndex 说明

`qaIndex` 是QA在历史数组中的索引：

```json
qaHistory = [
  {...},  // qaIndex = 0 (第1个QA)
  {...},  // qaIndex = 1 (第2个QA)
  {...}   // qaIndex = 2 (第3个QA)
]
```

**示例：**
- 要Pin第1个QA → `qaIndex: 0`
- 要Pin第2个QA → `qaIndex: 1`
- 要Pin第3个QA → `qaIndex: 2`

## 数据库表结构

### pins 表字段

| 字段 | 类型 | 说明 |
|------|------|------|
| pin_id | BIGINT | 主键 |
| user_id | BIGINT | 用户ID |
| source_type | VARCHAR(20) | CONTACT_QA/USER_QA |
| session_id | VARCHAR(100) | 会话ID |
| qa_index | INT | QA索引 |
| contact_id | BIGINT | 联系人ID（可选） |
| question | TEXT | 用户问题 |
| answer | TEXT | AI回答 |
| supplement_question | TEXT | 补充问题（可选） |
| supplement_answer | TEXT | 补充回答（可选） |
| needs_more_info | TINYINT | 是否有补充环节 |
| context_info | TEXT | 上下文JSON |
| note | VARCHAR(500) | 用户备注 |
| tags | VARCHAR(255) | 标签（逗号分隔） |
| qa_timestamp | VARCHAR(50) | QA发生时间 |
| created_at | DATETIME | Pin创建时间 |
| updated_at | DATETIME | 更新时间 |

### 索引

- `uk_user_session_qa` - 唯一索引（防重复Pin）
- `idx_user_id` - 用户查询优化
- `idx_source_type` - 类型筛选优化
- `idx_contact_id` - 联系人筛选优化
- `idx_created_at` - 时间排序优化

## 前端实现建议

### QA界面集成

在每个QA回答旁边显示Pin按钮：

```javascript
// 检查是否已Pin
const isPinned = await checkPinned(sessionId, qaIndex);

// 显示按钮
if (isPinned) {
  <Button disabled>✅ 已收藏</Button>
} else {
  <Button onClick={() => pinQA(sessionId, qaIndex)}>📌 收藏</Button>
}

// Pin操作
async function pinQA(sessionId, qaIndex) {
  const response = await fetch('/api/pins', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      sessionId,
      qaIndex,
      note: '',      // 可选，弹窗让用户输入
      tags: ''       // 可选，弹窗让用户输入
    })
  });
  
  if (response.ok) {
    showToast('收藏成功！');
  }
}
```

### Pin列表页面

```javascript
// 获取所有Pin
const pins = await fetch('/api/pins', {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

// 渲染列表
pins.data.forEach(pin => {
  <PinCard>
    <SourceBadge>{pin.sourceType}</SourceBadge>
    <ContactName>{pin.contactName || '自我QA'}</ContactName>
    <Question>{pin.questionPreview}</Question>
    <Answer>{pin.answerPreview}</Answer>
    <Tags>{pin.tags.join(', ')}</Tags>
    <CreatedAt>{formatDate(pin.createdAt)}</CreatedAt>
  </PinCard>
});
```

### Pin详情页面

点击Pin卡片查看完整内容：

```javascript
const pinDetail = await fetch(`/api/pins/${pinId}`, {
  headers: { 'Authorization': `Bearer ${token}` }
}).then(r => r.json());

// 显示完整Q&A
<div>
  <h3>问题</h3>
  <p>{pinDetail.question}</p>
  
  <h3>回答</h3>
  <p>{pinDetail.answer}</p>
  
  {pinDetail.hasSupplementInfo && (
    <>
      <h4>补充问题</h4>
      <p>{pinDetail.supplementQuestion}</p>
      
      <h4>补充回答</h4>
      <p>{pinDetail.supplementAnswer}</p>
    </>
  )}
  
  <h3>我的备注</h3>
  <p>{pinDetail.note}</p>
  
  <h3>标签</h3>
  <Tags>{pinDetail.tags}</Tags>
</div>
```

## 技术实现细节

### 1. 防重复机制

数据库唯一索引：`uk_user_session_qa (user_id, session_id, qa_index)`

当用户尝试Pin同一个QA时，会收到错误提示："This QA has already been pinned"

### 2. 权限验证

```java
// 验证会话所有权
if (!session.getUserId().equals(userId)) {
    throw new ApiException(ErrorCode.FORBIDDEN, "Cannot pin another user's conversation");
}
```

### 3. 上下文信息构建

```java
Map<String, Object> context = new HashMap<>();
context.put("sessionType", "联系人QA");
context.put("contactName", "张三");
context.put("sessionDate", "2025-10-18");

String contextInfo = objectMapper.writeValueAsString(context);
```

### 4. 标签处理

**存储：** 逗号分隔的字符串
```
"工作,重要,技巧"
```

**返回：** 解析为数组
```java
["工作", "重要", "技巧"]
```

### 5. 预览文本截断

```java
private String truncate(String text, int maxLength) {
    if (text == null) return "";
    if (text.length() <= maxLength) return text;
    return text.substring(0, maxLength) + "...";
}
```

- 问题预览：50字
- 回答预览：100字

## 使用示例

### 示例1：Pin一个简单的QA

**场景：** 用户问"张三的职业是什么？"，AI直接回答

```bash
# 创建Pin
curl -X POST http://localhost:8080/api/pins \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-123",
    "qaIndex": 0,
    "note": "职业信息",
    "tags": "基本信息"
  }'
```

### 示例2：Pin一个有补充问答的QA

**场景：** 用户问"我们平时怎么相处？"，AI要求补充信息

```bash
# Pin时会自动保存完整的补充问答流程
curl -X POST http://localhost:8080/api/pins \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "session-123",
    "qaIndex": 1,
    "tags": "相处模式,重要洞察"
  }'
```

**Pin将包含：**
- 原始问题："我们平时怎么相处？"
- 初始回答："需要更多信息..."
- 补充问题："请描述你们的日常互动"
- 补充回答："基于你的补充，你们的相处模式是..."

### 示例3：查看所有Pin

```bash
curl -X GET http://localhost:8080/api/pins \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例4：按联系人筛选

```bash
curl -X GET "http://localhost:8080/api/pins?contactId=5" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 示例5：更新Pin

```bash
curl -X PUT http://localhost:8080/api/pins/1 \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "note": "这个洞察帮助我改善了关系",
    "tags": "已实践,有效,关键"
  }'
```

## 常见问题

### Q1: 如何知道qaIndex是多少？

前端在显示QA历史时，应该给每个QA分配索引：

```javascript
qaHistory.forEach((qa, index) => {
  <QACard qaIndex={index}>
    <Question>{qa.question}</Question>
    <Answer>{qa.answer}</Answer>
    <PinButton qaIndex={index} />
  </QACard>
});
```

### Q2: 可以修改Pin的问题或回答吗？

不可以。Pin的Q&A内容是只读的，因为它来自历史记录。用户只能修改备注和标签。

### Q3: 删除会话会影响Pin吗？

不会。Pin独立存储，即使原始会话被删除，Pin仍然保留。但如果联系人被删除，`contact_id` 会被设置为 NULL（外键约束：ON DELETE SET NULL）。

### Q4: 可以Pin同一个QA多次吗？

不可以。数据库有唯一约束，同一用户的同一会话的同一QA只能Pin一次。

### Q5: 如何判断应该显示哪个回答？

对于有补充问答的QA：
- 列表预览：显示 `supplementAnswer`（最终回答）
- 详情页面：同时显示 `answer` 和 `supplementAnswer`

```java
String finalAnswer = pin.getNeedsMoreInfo() && pin.getSupplementAnswer() != null
    ? pin.getSupplementAnswer()
    : pin.getAnswer();
```

## 数据迁移

执行数据库迁移：

```bash
mvn flyway:migrate
```

或启动应用时自动执行（已配置 `spring.flyway.enabled=true`）。

## API端点总览

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/pins | 创建Pin |
| GET | /api/pins | 获取Pin列表 |
| GET | /api/pins/{pinId} | 获取Pin详情 |
| PUT | /api/pins/{pinId} | 更新Pin |
| DELETE | /api/pins/{pinId} | 删除Pin |
| GET | /api/pins/check | 检查是否已Pin |

## 性能考虑

### 索引优化

- 用户查询：`idx_user_id`
- 联系人筛选：`idx_user_contact (user_id, contact_id)`
- 类型筛选：`idx_source_type`
- 时间排序：`idx_created_at`

### 查询优化

所有列表查询都按 `created_at DESC` 排序，最新的Pin排在前面。

## 扩展功能建议

### 阶段1（已实现）
- ✅ 基本Pin CRUD
- ✅ 按联系人/类型筛选
- ✅ 防重复机制
- ✅ 备注和标签

### 阶段2（未来）
- 全文搜索Pin内容
- Pin导出（PDF/Markdown）
- Pin分类和文件夹
- Pin分享功能
- 统计分析（最常Pin的类型等）

## 安全性

1. **所有权验证** - 用户只能Pin自己的会话
2. **权限检查** - 不能访问其他用户的Pin
3. **数据隔离** - 通过user_id确保数据隔离
4. **外键约束** - 确保数据完整性

## 总结

Pin功能通过以下设计确保Q&A对应关系正确：

1. ✅ 使用 `qaIndex` 精确定位
2. ✅ 完整复制 `QaHistoryEntry` 所有字段
3. ✅ 保存补充问答的完整流程
4. ✅ 防止重复Pin（唯一索引）
5. ✅ 自动识别来源类型
6. ✅ 自动提取上下文信息

用户体验流畅，数据结构清晰，完全不需要手动输入Q&A内容！🎉

