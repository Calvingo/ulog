# 创建联系人 API 调用报告

## API 概述

此API用于创建新的联系人记录。每个联系人必须关联到一个已认证的用户，支持添加联系人的姓名、描述和AI摘要信息。

---

## 接口信息

### 基本信息
- **接口路径**: `/api/v1/contacts`
- **请求方法**: `POST`
- **认证方式**: Bearer Token (JWT)
- **Content-Type**: `application/json`

---

## 请求说明

### 1. 认证要求

此接口需要用户身份验证。在请求头中必须携带有效的JWT访问令牌：

```
Authorization: Bearer <access_token>
```

### 2. 请求参数

#### Request Body (JSON格式)

| 字段名 | 类型 | 必填 | 长度限制 | 说明 |
|--------|------|------|----------|------|
| name | String | ✅ 是 | 最大128字符 | 联系人姓名 |
| description | String | ❌ 否 | 最大1024字符 | 联系人描述信息 |
| aiSummary | String | ❌ 否 | 最大2048字符 | AI生成的联系人摘要 |

#### 参数验证规则
- `name`: 必填，不能为空白字符串，最大长度128字符
- `description`: 可选，最大长度1024字符
- `aiSummary`: 可选，最大长度2048字符

---

## 响应说明

### 成功响应

**HTTP状态码**: `201 Created`

**响应格式**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 1,
    "name": "张三",
    "description": "公司同事",
    "aiSummary": "这是一位在技术部门工作的同事，擅长后端开发",
    "createdAt": "2025-10-14T10:30:00",
    "updatedAt": "2025-10-14T10:30:00"
  },
  "traceId": "550e8400-e29b-41d4-a716-446655440000",
  "ts": 1728902400000
}
```

### 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| code | Integer | 响应码，0表示成功 |
| message | String | 响应消息 |
| data | Object | 响应数据对象 |
| data.id | Long | 联系人唯一标识ID |
| data.name | String | 联系人姓名 |
| data.description | String | 联系人描述 |
| data.aiSummary | String | AI摘要 |
| data.createdAt | DateTime | 创建时间 (ISO 8601格式) |
| data.updatedAt | DateTime | 更新时间 (ISO 8601格式) |
| traceId | String | 请求追踪ID，用于问题排查 |
| ts | Long | 响应时间戳（毫秒） |

---

## 调用示例

### 示例 1: 创建基本联系人

**请求示例 (cURL)**:
```bash
curl -X POST 'http://localhost:8080/api/v1/contacts' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "李四"
  }'
```

**请求示例 (JavaScript/Fetch)**:
```javascript
const response = await fetch('http://localhost:8080/api/v1/contacts', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    name: '李四'
  })
});

const result = await response.json();
console.log(result);
```

**请求示例 (Python)**:
```python
import requests

url = "http://localhost:8080/api/v1/contacts"
headers = {
    "Authorization": f"Bearer {access_token}",
    "Content-Type": "application/json"
}
data = {
    "name": "李四"
}

response = requests.post(url, headers=headers, json=data)
print(response.json())
```

### 示例 2: 创建包含完整信息的联系人

**请求示例**:
```bash
curl -X POST 'http://localhost:8080/api/v1/contacts' \
  -H 'Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "王五",
    "description": "大学同学，现在在互联网公司工作",
    "aiSummary": "一位优秀的产品经理，对用户体验有深刻理解，经常分享行业见解"
  }'
```

**成功响应**:
```json
{
  "code": 0,
  "message": "success",
  "data": {
    "id": 2,
    "name": "王五",
    "description": "大学同学，现在在互联网公司工作",
    "aiSummary": "一位优秀的产品经理，对用户体验有深刻理解，经常分享行业见解",
    "createdAt": "2025-10-14T14:25:30",
    "updatedAt": "2025-10-14T14:25:30"
  },
  "traceId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "ts": 1728916730000
}
```

---

## 错误处理

### 常见错误响应

#### 1. 缺少必填字段 (400 Bad Request)

**错误场景**: name字段为空或未提供

```json
{
  "code": 400,
  "message": "name is required",
  "data": null,
  "traceId": "550e8400-e29b-41d4-a716-446655440001",
  "ts": 1728902400000
}
```

#### 2. 字段长度超限 (400 Bad Request)

**错误场景**: name超过128字符

```json
{
  "code": 400,
  "message": "name max length 128",
  "data": null,
  "traceId": "550e8400-e29b-41d4-a716-446655440002",
  "ts": 1728902400000
}
```

#### 3. 未认证 (401 Unauthorized)

**错误场景**: 未提供或提供了无效的JWT令牌

```json
{
  "code": 401,
  "message": "Unauthorized",
  "data": null,
  "traceId": "550e8400-e29b-41d4-a716-446655440003",
  "ts": 1728902400000
}
```

#### 4. 令牌过期 (401 Unauthorized)

**错误场景**: JWT访问令牌已过期

```json
{
  "code": 401,
  "message": "Token expired",
  "data": null,
  "traceId": "550e8400-e29b-41d4-a716-446655440004",
  "ts": 1728902400000
}
```

### 错误码说明

| HTTP状态码 | 业务码 | 说明 | 处理建议 |
|-----------|--------|------|----------|
| 201 | 0 | 成功 | - |
| 400 | 400 | 请求参数错误 | 检查请求参数格式和内容 |
| 401 | 401 | 未认证或令牌无效 | 重新登录获取新的访问令牌 |
| 403 | 403 | 无权限 | 确认用户权限 |
| 500 | 500 | 服务器内部错误 | 联系技术支持，提供traceId |

---

## 注意事项

### 1. 认证令牌管理
- **访问令牌有效期**: 15分钟
- **刷新令牌有效期**: 14天
- 当访问令牌过期时，使用刷新令牌获取新的访问令牌
- 建议在客户端实现自动刷新令牌机制

### 2. 数据限制
- 每个用户创建的联系人数量无限制（取决于数据库容量）
- 字段长度必须严格遵守限制，超长数据会被拒绝

### 3. 性能建议
- 批量创建联系人时，建议逐个调用而非并发过多请求
- 推荐客户端实现请求重试机制（指数退避策略）

### 4. 安全建议
- 始终使用HTTPS协议传输数据
- 不要在URL中暴露敏感信息
- 妥善保管JWT令牌，不要存储在不安全的位置
- 定期刷新令牌以提高安全性

### 5. 调试建议
- 遇到错误时，记录响应中的 `traceId`，便于问题追踪
- 检查请求头中的 `Content-Type` 是否正确设置
- 确保JSON格式正确，字段名大小写敏感

---

## 测试环境

### 开发环境配置
- **基础URL**: `http://localhost:8080`
- **数据库**: MySQL (默认端口3306)
- **认证**: JWT (密钥可在application.properties中配置)

### 获取测试令牌

首先需要通过登录接口获取访问令牌：

```bash
# 1. 注册用户
curl -X POST 'http://localhost:8080/api/v1/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "password": "Test123456",
    "email": "test@example.com"
  }'

# 2. 登录获取令牌
curl -X POST 'http://localhost:8080/api/v1/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "password": "Test123456"
  }'

# 响应示例
{
  "code": 0,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "accessTokenExpiresAt": "2025-10-14T15:45:00",
    "refreshTokenExpiresAt": "2025-10-28T15:30:00"
  },
  "traceId": "...",
  "ts": 1728902400000
}
```

---

## Postman 集合

项目根目录下的 `complete_api_postman_collection.json` 文件包含了完整的API测试集合，可直接导入Postman使用。

### 导入步骤：
1. 打开Postman
2. 点击 `Import` 按钮
3. 选择 `complete_api_postman_collection.json` 文件
4. 导入后在集合变量中设置 `baseUrl` 和 `accessToken`

---

## 相关API

- **获取联系人列表**: `GET /api/v1/contacts`
- **获取单个联系人**: `GET /api/v1/contacts/{cid}`
- **更新联系人**: `PATCH /api/v1/contacts/{cid}`
- **删除联系人**: `DELETE /api/v1/contacts/{cid}`
- **用户认证**: `/api/v1/auth/login`
- **刷新令牌**: `/api/v1/auth/refresh`

---

## 技术栈

- **后端框架**: Spring Boot 3.x
- **数据库**: MySQL 8.0+
- **ORM**: JPA/Hibernate
- **认证**: JWT (JSON Web Token)
- **API文档**: OpenAPI 3.0 (Swagger)

---

## 联系支持

如有问题或建议，请通过以下方式联系：
- 查看项目 `HELP.md` 文件
- 使用 `traceId` 报告问题以便快速定位

---

**文档版本**: v1.0  
**最后更新**: 2025-10-14  
**API版本**: v1

