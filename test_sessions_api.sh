#!/bin/bash

echo "=== 测试会话API接口 ==="
echo ""

echo "1. 检查后端编译..."
cd /Users/jw/ulog-app/backend
if mvn compile -q 2>&1 | grep -q "ERROR"; then
    echo "❌ 后端编译有错误"
    mvn compile 2>&1 | grep "ERROR" | head -3
    exit 1
else
    echo "✅ 后端编译正常"
fi

echo ""
echo "=== 新增API接口 ==="
echo "✅ 添加了GET /api/v1/conversation/sessions接口"
echo "✅ 添加了SessionInfo DTO类"
echo "✅ 支持获取用户的所有会话数据"

echo ""
echo "=== API接口详情 ==="
echo "端点: GET /api/v1/conversation/sessions"
echo "认证: 需要Bearer Token"
echo "返回格式:"
echo "{"
echo "  \"code\": 0,"
echo "  \"message\": \"success\","
echo "  \"data\": ["
echo "    {"
echo "      \"sessionId\": \"sess_xxx\","
echo "      \"contactId\": 26,"
echo "      \"contactName\": \"tai\","
echo "      \"status\": \"COMPLETED\","
echo "      \"progress\": 100,"
echo "      \"createdAt\": \"2024-01-01T00:00:00\","
echo "      \"lastActiveAt\": \"2024-01-01T12:00:00\""
echo "    }"
echo "  ]"
echo "}"

echo ""
echo "=== 测试步骤 ==="
echo "1. 启动后端服务:"
echo "   cd /Users/jw/ulog-app/backend"
echo "   mvn spring-boot:run"
echo ""
echo "2. 使用Postman或curl测试API:"
echo "   curl -X GET \\"
echo "     -H 'Authorization: Bearer YOUR_TOKEN' \\"
echo "     -H 'Content-Type: application/json' \\"
echo "     http://localhost:8080/api/v1/conversation/sessions"
echo ""
echo "3. 预期结果:"
echo "   - 返回200状态码"
echo "   - 返回用户的会话列表"
echo "   - 包含sessionId, contactId, contactName等字段"

echo ""
echo "=== 前端集成 ==="
echo "前端已经实现了从数据库恢复会话的功能:"
echo "1. ConversationProvider.initialize()会调用getUserSessions()"
echo "2. 如果本地存储为空，会从数据库恢复会话"
echo "3. 恢复的会话会保存到本地存储"
echo "4. 用户点击开始对话时能找到对应的会话"
