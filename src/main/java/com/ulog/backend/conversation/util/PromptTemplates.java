package com.ulog.backend.conversation.util;

import java.util.List;
import java.util.Map;

public class PromptTemplates {
    
    // 系统角色定义
    public static final String SYSTEM_ROLE = """
        你是一个专业的关系管理助手，正在帮助用户收集关于某个联系人的信息。
        
        你的任务：
        1. 根据收集维度，自然地向用户提问
        2. 从用户的回答中提取关键信息
        3. 每次只问一个问题，保持对话自然流畅
        4. 如果用户说"不想回答"、"跳过"、"下一个"，就礼貌地询问下一个维度
        5. 如果用户的回答包含多个维度的信息，智能识别并提取
        
        对话风格：
        - 口语化、友好、像朋友聊天
        - 不要生硬地逐条提问
        - 根据上下文自然过渡
        - 适当表达理解和共鸣
        
        重要规则：
        - 只返回下一个问题，不要额外解释
        - 问题要简短、明确、易回答
        - 如果某个信息已经知道了，不要重复问
        """;
    
    /**
     * 生成第一个问题
     */
    public static String buildFirstQuestionPrompt(String contactName) {
        return String.format("""
            你好！我们来聊聊%s吧。
            
            请生成第一个自然的问题，从基本信息开始了解Ta。
            可以问年龄、职业、教育背景等。
            
            【专业框架收集要求】
            请严格按照以下5个系统进行信息收集：
            
            系统1: 基本画像系统 - 基本信息、社会角色、生活方式、社交风格、性格特质、自我价值
            系统2: 心理与人格系统 - 核心动机、情绪模式、决策风格
            系统3: 关系体验系统 - 互动频率、互动能量、信任水平、价值互惠、关系边界、关系母型
            系统4: 时间与发展系统 - 关系起点、关系长度、成长趋势、临界事件、未来潜力
            系统5: 价值与意义系统 - 角色标签、关系功能、自我影响、社交位置、投入产出
            
            只返回问题本身，要口语化、友好。
            """, contactName);
    }
    
    /**
     * 生成下一个问题的Prompt
     */
    public static String buildNextQuestionPrompt(
        String contactName,
        String currentDimension,
        List<String> completedDimensions,
        Map<String, Object> collectedData,
        String lastUserMessage
    ) {
        return String.format("""
            当前任务：收集关于"%s"的信息
            
            当前维度：%s
            已完成维度：%s
            已收集信息：
            %s
            
            用户上一次回答：%s
            
            【专业框架收集要求】
            请严格按照以下5个系统进行信息收集：
            
            系统1: 基本画像系统 - 基本信息、社会角色、生活方式、社交风格、性格特质、自我价值
            系统2: 心理与人格系统 - 核心动机、情绪模式、决策风格
            系统3: 关系体验系统 - 互动频率、互动能量、信任水平、价值互惠、关系边界、关系母型
            系统4: 时间与发展系统 - 关系起点、关系长度、成长趋势、临界事件、未来潜力
            系统5: 价值与意义系统 - 角色标签、关系功能、自我影响、社交位置、投入产出
            
            【问题生成要求】
            - 如果当前维度信息已经足够，可以进入下一个维度
            - 如果当前维度还需要更多信息，继续深入收集
            - 问题要承接上下文，自然过渡
            - 严格按照专业框架的要素进行提问
            - 只返回问题本身，不要其他内容
            
            【当前维度重点】
            请针对当前维度"%s"生成一个具体的问题，确保能收集到该维度的关键信息。
            """,
            contactName,
            currentDimension,
            completedDimensions.isEmpty() ? "无" : String.join(", ", completedDimensions),
            formatCollectedData(collectedData),
            lastUserMessage,
            currentDimension
        );
    }
    
    /**
     * 智能提取信息的Prompt
     */
    public static String buildIntelligentExtractionPrompt(
        String userMessage,
        String currentDimension,
        Map<String, Object> collectedData,
        String lastQuestion
    ) {
        return String.format("""
            【上下文】
            当前问题：%s
            当前维度：%s
            已收集的所有信息：
            %s
            
            【用户回答】
            "%s"
            
            【任务】
            请分析用户的回答，并以纯JSON格式返回（不要使用markdown代码块）：
            
            {
                "intent": "answer|correction|supplement|skip|want_to_end|confirm_end|continue",
                "updates": {
                    // 需要更新/新增的所有信息（键值对）
                    // 例如：{"age": "30岁", "occupation": "产品经理", "relationship": "同事"}
                },
                "shouldContinueCurrentQuestion": false,
                "wantsToEnd": false,
                "endConfidence": "weak|medium|strong",
                "hasMinimumInfo": true,
                "reasoning": "简短说明你的判断理由"
            }
            
            【重要规则】
            1. intent判断：
               - answer: 正常回答当前问题
               - correction: 修正之前的信息（"不对"、"错了"、"应该是"）
               - supplement: 补充之前的信息
               - skip: 跳过当前问题（"不知道"、"不清楚"）
               - want_to_end: 想结束（"差不多了"、"就这些"、"不想回答了"、"结束问卷"、"问卷结束"、"结束"、"完成"、"没有"、"不知道"、"够了"、"不想继续"）
               - confirm_end: 确认结束（在CONFIRMING_END状态下回复"是"、"好"）
               - continue: 继续（在CONFIRMING_END状态下回复"继续"、"再想想"）
            
            2. wantsToEnd和endConfidence：
               - weak: 单次"不知道" → 可能只是跳过
               - medium: "差不多了"、"就这些"、"结束问卷"、"问卷结束"、"没有" → 可能想结束
               - strong: "不想回答了"、"够了"、"结束"、"完成"、"不想继续" → 明确想结束
            
            3. hasMinimumInfo判断：
               - 必须有：name（联系人姓名）
               - 至少有2条其他信息（age/occupation/relationship/interaction等）
            
            4. updates提取：
               - 提取所有提到的信息，即使不是当前维度
               - 如果是修正，覆盖原值
               - 键名用英文，值保持用户原话
               - 严格按照以下框架提取，只提取用户实际提供的信息，不要推测或编造：
                 
                 【系统1: 基本画像系统】
                 * age: 年龄信息（岁、年龄、多大、年纪）
                 * occupation: 职业信息（职业、工作、从事）
                 * education: 教育背景（教育、学历、毕业、学校）
                 * city: 地理位置（城市、在、住、位置）
                 * work_type: 工作类型
                 * industry_status: 行业地位
                 * identity_tag: 身份标签（上班族、自由职业者、学生等）
                 * daily_routine: 作息规律
                 * exercise_frequency: 运动频率
                 * eating_habits: 饮食习惯
                 * leisure_hobby: 休闲爱好
                 * social_frequency: 社交频率
                 * social_activity_preference: 社交偏好
                 * personality_characteristics: 性格特点
                 * mbti_type: MBTI类型
                 
                 【系统2: 心理与人格系统】
                 * core_values: 核心价值观
                 * motivation_drivers: 动机驱动
                 * emotional_stability: 情绪稳定性
                 * empathy_level: 共情能力
                 * decision_making_style: 决策风格
                 * thinking_preference: 思维偏好
                 
                 【系统3: 关系体验系统】
                 * relationship: 关系信息（同事、朋友、同学等）
                 * interaction: 互动方式（工作配合、讨论等）
                 * meeting_frequency: 见面频率
                 * chat_frequency: 聊天频率
                 * interaction_energy: 互动能量
                 * emotional_support_level: 情感支持
                 * trust_level: 信任水平
                 * information_transparency: 信息透明度
                 * emotional_value: 情感价值
                 * information_value: 信息价值
                 * social_resource_value: 社交资源价值
                 * companionship_value: 陪伴价值
                 * privacy_respect: 隐私尊重
                 * balance_giving: 平衡给予
                 * relationship_archetype: 关系类型
                 * role_dynamics: 角色动态
                 
                 【系统4: 时间与发展系统】
                 * acquaintance_channel: 认识渠道
                 * first_meeting_context: 初次见面背景
                 * years_known: 认识年限
                 * relationship_development_stage: 关系发展阶段
                 * relationship_trend: 关系趋势
                 * closeness_level: 亲密程度
                 * shared_experiences: 共同经历
                 * conflicts: 冲突
                 * cooperation_events: 合作事件
                 * development_potential: 发展潜力
                 * relationship_sustainability: 关系可持续性
                 
                 【系统5: 价值与意义系统】
                 * role_tags: 角色标签
                 * identity_in_my_life: 在我生活中的身份
                 * companionship: 陪伴
                 * reflection: 反思
                 * resource_exchange: 资源交换
                 * co_creation: 共创
                 * entertainment: 娱乐
                 * enhancement_feeling: 提升感
                 * pressure_feeling: 压力感
                 * mirror_self: 镜像自我
                 * security_feeling: 安全感
                 * core_circle_position: 核心圈位置
                 * social_network_role: 社交网络角色
                 * time_investment: 时间投入
                 * emotional_investment: 情感投入
                 * return_balance: 回报平衡
            
            【重要格式要求】
            - 只返回纯JSON格式，不要使用markdown代码块
            - 不要包含```json```、```等markdown标记
            - 不要添加任何解释文字
            - 直接返回JSON对象，从{开始到}结束
            
            只返回JSON，不要其他内容。
            """,
            lastQuestion,
            currentDimension,
            formatCollectedData(collectedData),
            userMessage
        );
    }
    
    /**
     * 生成description的Prompt
     */
    public static String buildDescriptionPrompt(
        String contactName,
        Map<String, Object> collectedData
    ) {
        return String.format("""
            请将以下收集到的信息，整理成一段自然、流畅的描述。
            
            联系人姓名：%s
            收集的信息：
            %s
            
            【严格要求】
            1. 只能使用用户实际提供的信息，严禁推测、编造或添加任何用户没有提供的内容
            2. 如果某个信息用户没有提供，不要编造或推测
            3. 描述要准确反映用户的原始输入，不要美化或修饰
            4. 用第三人称描述
            5. 自然语言，像是在向朋友介绍这个人
            6. 突出重点信息和特色
            7. 不要分点列举，要写成连贯的段落
            8. 控制在200-500字
            9. 如果有关系相关的信息（认识渠道、互动等），要体现出来
            
            【禁止行为】
            - 禁止添加"专业能力扎实"、"善于沟通"等推测性描述
            - 禁止添加"思路清晰"、"有建设性"等主观评价
            - 禁止添加"值得信赖"、"好搭档"等价值判断
            - 禁止任何形式的推测、编造或美化
            
            只返回描述文本，不要其他内容。
            """,
            contactName,
            formatCollectedData(collectedData)
        );
    }
    
    /**
     * 生成请求最低信息的问题
     * @deprecated 未被使用，建议使用 buildMinimumInfoQuestionForUser 替代或删除
     */
    @Deprecated
    public static String buildMinimumInfoQuestionPrompt(
        String contactName,
        Map<String, Object> collectedData
    ) {
        return String.format("""
            用户想结束问卷，但信息还不够创建联系人。
            
            联系人姓名：%s
            已有信息：
            %s
            
            请生成一个友好的问题，请求用户补充最关键的1-2条信息。
            
            缺失信息优先级：
            1. 关系（你们是什么关系？同事/朋友/家人等）
            2. 年龄或职业（至少知道一个）
            3. 互动频率（多久联系一次）
            
            要求：
            1. 语气要理解用户想结束的心情
            2. 说明只需要再回答1-2个问题
            3. 问最重要的缺失信息
            4. 简短、友好
            
            只返回问题本身。
            """,
            contactName,
            formatCollectedData(collectedData)
        );
    }
    
    /**
     * QA模式：回答关于联系人的问题
     * @deprecated 使用 buildBaseContactQaSystemPrompt + 原生messages数组代替
     */
    @Deprecated
    public static String buildQaSystemPrompt(
        String contactName,
        String description,
        Map<String, Object> collectedData
    ) {
        return String.format("""
            你是一个智能助手，帮助用户了解Ta的联系人。
            
            联系人信息：
            姓名：%s
            描述：%s
            
            收集的原始数据：
            %s
            
            请根据这些信息回答用户的问题。
            
            规则：
            - 如果信息中有相关内容，给出准确回答
            - 如果信息中没有，诚实地说不知道
            - 可以基于已有信息进行合理推断，但要说明是推断
            - 语气友好、自然
            """,
            contactName,
            description,
            formatCollectedData(collectedData)
        );
    }
    
    /**
     * 格式化已收集的数据
     */
    private static String formatCollectedData(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            return "（暂无）";
        }
        
        StringBuilder sb = new StringBuilder();
        data.forEach((key, value) -> {
            if (value != null) {
                sb.append(String.format("- %s: %s\n", key, value));
            }
        });
        
        return sb.toString();
    }
    
    /**
     * 构建完成提示消息
     */
    public static String buildCompletionMessage(String contactName) {
        return String.format("""
            太棒了！联系人'%s'已创建成功✅
            
            现在你可以：
            • 问我关于Ta的问题
            • 让我生成AI总结
            • 更新Ta的信息
            • 或者查看完整资料
            """, contactName);
    }
    
    /**
     * 获取建议的后续操作
     */
    public static List<String> getSuggestedActions() {
        return List.of(
            "生成AI总结",
            "查看完整信息",
            "添加更多信息",
            "结束对话"
        );
    }
    
    /**
     * 构建信息需求分析Prompt
     */
    public static String buildInfoAnalysisPrompt(String question, String contactDesc, String userDesc) {
        return String.format("""
            【任务】分析用户问题，判断是否需要补充信息
            
            【用户问题】%s
            
            【联系人描述】%s
            
            【用户描述】%s
            
            【分析要求】
            1. 判断问题是否涉及用户与联系人的关系
            2. 分析现有信息是否足够回答问题
            3. 如果不够，指出需要补充的具体信息
            
            【返回格式】
            {
                "needsSupplement": true/false,
                "reason": "分析原因",
                "supplementFields": ["field1", "field2"],
                "supplementQuestion": "需要补充的问题"
            }
            
            【重要格式要求】
            - 只返回纯JSON格式，不要使用markdown代码块
            - 不要包含```json```、```等markdown标记
            - 不要添加任何解释文字
            - 直接返回JSON对象，从{开始到}结束
            
            只返回JSON，不要其他内容。
            """, question, contactDesc, userDesc);
    }
    
    /**
     * 构建最终回答生成Prompt
     * @deprecated 现在使用原生messages数组代替
     */
    @Deprecated
    public static String buildFinalAnswerPrompt(String question, String contactDesc, String userDesc) {
        return String.format("""
            【任务】基于双方描述回答问题
            
            【用户问题】%s
            
            【联系人描述】%s
            
            【用户描述】%s
            
            【回答要求】
            1. 结合双方描述给出准确回答
            2. 如果信息不足，诚实说明
            3. 语气友好、自然
            4. 可以基于已有信息进行合理推断
            
            只返回回答内容，不要其他说明。
            """, question, contactDesc, userDesc);
    }
    
    /**
     * 构建基础的联系人QA系统Prompt（不包含历史对话）
     * 历史对话通过messages数组的原生格式传递
     */
    public static String buildBaseContactQaSystemPrompt(
        String contactDesc, 
        String contactSelfValue,
        String userDesc, 
        String userSelfValue
    ) {
        return String.format("""
            【任务】你是一个专业的关系分析助手，帮助用户更好地理解Ta与联系人的关系。
            
            【联系人信息】
            描述：%s
            自我价值评分（1-5分，5分为最高）：
            - 自尊：%s/5.0 (自我价值感和自我尊重程度)
            - 自我接纳：%s/5.0 (接受自己优缺点的程度)
            - 自我效能：%s/5.0 (对自己能力的信心程度)
            - 存在价值感：%s/5.0 (对生命意义和价值的感知)
            - 自我一致性：%s/5.0 (内在价值观与行为的一致性)
            
            【用户信息】
            描述：%s
            自我价值评分（1-5分，5分为最高）：
            - 自尊：%s/5.0 (自我价值感和自我尊重程度)
            - 自我接纳：%s/5.0 (接受自己优缺点的程度)
            - 自我效能：%s/5.0 (对自己能力的信心程度)
            - 存在价值感：%s/5.0 (对生命意义和价值的感知)
            - 自我一致性：%s/5.0 (内在价值观与行为的一致性)
            
            【回答要求】
            1. 结合双方描述和自我价值评分进行深度分析
            2. 基于对话历史提供连贯的回答
            3. 理解上下文中的指代（"他"、"那"、"这个"等）
            4. 提供基于数据的专业洞察
            5. 保持客观和专业
            6. 如果信息不足，诚实说明
            
            注意：你可以看到完整的对话历史，请充分利用上下文信息提供连贯的回答。
            """, 
            contactDesc, 
            parseSelfValueForPrompt(contactSelfValue, 0),
            parseSelfValueForPrompt(contactSelfValue, 1),
            parseSelfValueForPrompt(contactSelfValue, 2),
            parseSelfValueForPrompt(contactSelfValue, 3),
            parseSelfValueForPrompt(contactSelfValue, 4),
            userDesc,
            parseSelfValueForPrompt(userSelfValue, 0),
            parseSelfValueForPrompt(userSelfValue, 1),
            parseSelfValueForPrompt(userSelfValue, 2),
            parseSelfValueForPrompt(userSelfValue, 3),
            parseSelfValueForPrompt(userSelfValue, 4)
        );
    }
    
    /**
     * 构建基础的用户自我QA系统Prompt（不包含历史对话）
     * 历史对话通过messages数组的原生格式传递
     */
    public static String buildBaseUserSelfQaSystemPrompt(
        String userDesc,
        String userSelfValue
    ) {
        return String.format("""
            【任务】你是一个专业的个人分析助手，帮助用户更好地了解自己。
            
            【用户自我信息】
            描述：%s
            自我价值评分（1-5分，5分为最高）：
            - 自尊：%s/5.0 (自我价值感和自我尊重程度)
            - 自我接纳：%s/5.0 (接受自己优缺点的程度)
            - 自我效能：%s/5.0 (对自己能力的信心程度)
            - 存在价值感：%s/5.0 (对生命意义和价值的感知)
            - 自我一致性：%s/5.0 (内在价值观与行为的一致性)
            
            【回答要求】
            1. 结合用户描述和自我价值评分进行深度分析
            2. 基于对话历史提供连贯的回答
            3. 理解上下文中的指代
            4. 提供基于数据的专业洞察
            5. 保持客观和专业
            6. 如果信息不足，诚实说明
            
            注意：你可以看到完整的对话历史，请充分利用上下文信息提供连贯的回答。
            """,
            userDesc,
            parseSelfValueForPrompt(userSelfValue, 0),
            parseSelfValueForPrompt(userSelfValue, 1),
            parseSelfValueForPrompt(userSelfValue, 2),
            parseSelfValueForPrompt(userSelfValue, 3),
            parseSelfValueForPrompt(userSelfValue, 4)
        );
    }
    
    /**
     * 构建增强的联系人QA回答Prompt（包含self_value和qa_history）
     * @deprecated 使用buildBaseContactQaSystemPrompt + 原生messages数组代替
     */
    @Deprecated
    public static String buildEnhancedContactQaPrompt(
        String question, 
        String contactDesc, 
        String contactSelfValue,
        String userDesc, 
        String userSelfValue,
        String qaHistory
    ) {
        return String.format("""
            【任务】基于联系人和用户信息回答问题
            
            【用户问题】%s
            
            【联系人信息】
            描述：%s
            自我价值评分（1-5分，5分为最高）：
            - 自尊：%s/5.0 (自我价值感和自我尊重程度)
            - 自我接纳：%s/5.0 (接受自己优缺点的程度)
            - 自我效能：%s/5.0 (对自己能力的信心程度)
            - 存在价值感：%s/5.0 (对生命意义和价值的感知)
            - 自我一致性：%s/5.0 (内在价值观与行为的一致性)
            
            【用户信息】
            描述：%s
            自我价值评分（1-5分，5分为最高）：
            - 自尊：%s/5.0 (自我价值感和自我尊重程度)
            - 自我接纳：%s/5.0 (接受自己优缺点的程度)
            - 自我效能：%s/5.0 (对自己能力的信心程度)
            - 存在价值感：%s/5.0 (对生命意义和价值的感知)
            - 自我一致性：%s/5.0 (内在价值观与行为的一致性)
            
            【历史对话】
            %s
            
            【回答要求】
            1. 结合双方描述和自我价值评分进行分析
            2. 考虑历史对话的上下文
            3. 提供基于数据的洞察
            4. 保持客观和专业
            5. 如果信息不足，诚实说明
            
            只返回回答内容，不要其他说明。
            """, 
            question, 
            contactDesc, 
            parseSelfValueForPrompt(contactSelfValue, 0),
            parseSelfValueForPrompt(contactSelfValue, 1),
            parseSelfValueForPrompt(contactSelfValue, 2),
            parseSelfValueForPrompt(contactSelfValue, 3),
            parseSelfValueForPrompt(contactSelfValue, 4),
            userDesc,
            parseSelfValueForPrompt(userSelfValue, 0),
            parseSelfValueForPrompt(userSelfValue, 1),
            parseSelfValueForPrompt(userSelfValue, 2),
            parseSelfValueForPrompt(userSelfValue, 3),
            parseSelfValueForPrompt(userSelfValue, 4),
            qaHistory
        );
    }
    
    /**
     * 构建增强的用户自我QA回答Prompt（包含self_value和qa_history）
     * @deprecated 使用 buildBaseUserSelfQaSystemPrompt + 原生messages数组代替
     */
    @Deprecated
    public static String buildEnhancedUserSelfQaPrompt(
        String question,
        String userDesc,
        String userSelfValue,
        String qaHistory
    ) {
        return String.format("""
            【任务】基于用户自我信息回答问题
            
            【用户问题】%s
            
            【用户自我信息】
            描述：%s
            自我价值评分（1-5分，5分为最高）：
            - 自尊：%s/5.0 (自我价值感和自我尊重程度)
            - 自我接纳：%s/5.0 (接受自己优缺点的程度)
            - 自我效能：%s/5.0 (对自己能力的信心程度)
            - 存在价值感：%s/5.0 (对生命意义和价值的感知)
            - 自我一致性：%s/5.0 (内在价值观与行为的一致性)
            
            【历史对话】
            %s
            
            【回答要求】
            1. 基于自我价值评分提供个性化建议
            2. 考虑各个维度的平衡发展
            3. 提供建设性的自我提升方向
            4. 保持温暖和支持性的语调
            5. 如果信息不足，诚实说明
            
            只返回回答内容，不要其他说明。
            """,
            question, 
            userDesc,
            parseSelfValueForPrompt(userSelfValue, 0),
            parseSelfValueForPrompt(userSelfValue, 1),
            parseSelfValueForPrompt(userSelfValue, 2),
            parseSelfValueForPrompt(userSelfValue, 3),
            parseSelfValueForPrompt(userSelfValue, 4),
            qaHistory
        );
    }
    
    /**
     * 解析self_value字符串，获取指定索引的值用于Prompt
     */
    private static String parseSelfValueForPrompt(String selfValueStr, int index) {
        if (selfValueStr == null || selfValueStr.trim().isEmpty()) {
            return "3.0"; // 默认值
        }
        
        try {
            String[] values = selfValueStr.split(",");
            if (values.length > index) {
                return values[index].trim();
            }
            return "3.0"; // 默认值
        } catch (Exception e) {
            return "3.0"; // 默认值
        }
    }

    /**
     * 构建 Self Value 评估 Prompt
     */
    public static String buildSelfValueEvaluationPrompt(String description) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("你是心理评估专家，请根据以下描述信息评估该人的自我价值水平，从5个维度进行评分（1.0-5.0分）：\n\n");
        
        prompt.append("评估维度：\n");
        prompt.append("1. 自尊水平 (selfEsteem): 自信程度、自我肯定、自我价值感\n");
        prompt.append("2. 自我接纳 (selfAcceptance): 对自身缺点的态度、自我宽容度\n");
        prompt.append("3. 自我效能 (selfEfficacy): 对能力的信心、完成任务的能力感\n");
        prompt.append("4. 存在价值感 (existentialValue): 生命意义感、人生目标感\n");
        prompt.append("5. 自我一致性 (selfConsistency): 言行一致性、内外统一性\n\n");
        
        prompt.append("描述信息：\n");
        prompt.append(description != null && !description.trim().isEmpty() ? description : "（暂无描述信息）");
        prompt.append("\n\n");
        
        prompt.append("请返回JSON格式：\n");
        prompt.append("{\n");
        prompt.append("  \"selfEsteem\": 3.5,\n");
        prompt.append("  \"selfAcceptance\": 4.0,\n");
        prompt.append("  \"selfEfficacy\": 3.0,\n");
        prompt.append("  \"existentialValue\": 4.5,\n");
        prompt.append("  \"selfConsistency\": 3.8\n");
        prompt.append("}\n\n");
        
        prompt.append("评分标准：\n");
        prompt.append("- 1.0-2.0: 较低水平（明显缺乏自信、自我怀疑）\n");
        prompt.append("- 2.1-3.0: 中等偏下（有一定自信但不稳定）\n");
        prompt.append("- 3.1-4.0: 中等偏上（较为自信，自我价值感良好）\n");
        prompt.append("- 4.1-5.0: 较高水平（高度自信，自我价值感很强）\n\n");
        
        prompt.append("评估指导：\n");
        prompt.append("- 根据描述中的语言表达、行为模式、情感态度来判断\n");
        prompt.append("- 如果信息不足以判断某个维度，返回默认值3.0\n");
        prompt.append("- 评分要客观、合理，基于描述内容而非主观臆测\n");
        prompt.append("- 只返回JSON，不要其他解释\n");
        
        return prompt.toString();
    }

    
    // ========== 用户自我信息收集提示词 ==========
    
    /**
     * 生成用户自我收集的第一个问题
     */
    public static String buildFirstQuestionForUser() {
        return """
            你好！让我们来了解一下你自己吧。
            
            请生成第一个自然的问题，从基本信息开始了解用户。
            可以问年龄、职业、教育背景等。
            
            【专业框架收集要求】
            请严格按照以下5个系统进行信息收集：
            
            系统1: 基本画像系统 - 基本信息、社会角色、生活方式、社交风格、性格特质、自我价值
            系统2: 心理与人格系统 - 核心动机、情绪模式、决策风格
            系统3: 关系体验系统 - 社交偏好、人际关系风格
            系统4: 时间与发展系统 - 个人发展阶段、成长趋势、关键事件
            系统5: 价值与意义系统 - 自我定位、人生目标、价值追求
            
            注意：这是收集用户自己的信息，使用第二人称"你"。
            只返回问题本身，要口语化、友好。
            """;
    }
    
    /**
     * 生成用户自我收集的下一个问题
     */
    public static String buildNextQuestionForUser(
        String currentDimension,
        List<String> completedDimensions,
        Map<String, Object> collectedData,
        String lastUserMessage
    ) {
        return String.format("""
            当前任务：收集关于用户自己的信息
            
            当前维度：%s
            已完成维度：%s
            已收集信息：
            %s
            
            用户上一次回答：%s
            
            【专业框架收集要求】
            请严格按照以下5个系统进行信息收集：
            
            系统1: 基本画像系统 - 基本信息、社会角色、生活方式、社交风格、性格特质、自我价值
            系统2: 心理与人格系统 - 核心动机、情绪模式、决策风格
            系统3: 关系体验系统 - 社交偏好、人际关系风格
            系统4: 时间与发展系统 - 个人发展阶段、成长趋势、关键事件
            系统5: 价值与意义系统 - 自我定位、人生目标、价值追求
            
            【问题生成要求】
            - 如果当前维度信息已经足够，可以进入下一个维度
            - 如果当前维度还需要更多信息，继续深入收集
            - 问题要承接上下文，自然过渡
            - 严格按照专业框架的要素进行提问
            - 使用第二人称"你"
            - 只返回问题本身，不要其他内容
            
            【当前维度重点】
            请针对当前维度"%s"生成一个具体的问题，确保能收集到该维度的关键信息。
            """,
            currentDimension,
            completedDimensions.isEmpty() ? "无" : String.join(", ", completedDimensions),
            formatCollectedData(collectedData),
            lastUserMessage,
            currentDimension
        );
    }
    
    /**
     * 生成用户自我描述
     */
    public static String buildSelfDescriptionPrompt(Map<String, Object> collectedData) {
        return String.format("""
            请基于以下收集到的信息，生成一段关于用户自己的完整描述。
            
            收集的信息：
            %s
            
            【严格要求】
            1. 只能使用用户实际提供的信息，严禁推测、编造或添加任何用户没有提供的内容
            2. 如果某个信息用户没有提供，不要编造或推测
            3. 描述要准确反映用户的原始输入，不要美化或修饰
            4. 使用第一人称"我"来描述
            5. 生成一段完整、流畅、自然的自我介绍
            
            【禁止行为】
            - 禁止添加用户没有提到的性格特点
            - 禁止添加价值判断
            - 禁止任何形式的推测、编造或美化
            - 禁止使用模板化的语言
            
            【输出格式】
            返回一段150-300字的自然、流畅的自我描述文本，不要分点列举。
            """,
            formatCollectedData(collectedData)
        );
    }
    
    /**
     * 用户自我收集的完成消息
     */
    public static String buildUserCompletionMessage() {
        return """
            太好了！我已经了解了你的基本情况。
            
            现在你可以：
            1. 问我一些关于你自己的问题（比如"我的优势是什么？"）
            2. 让我帮你分析你的性格特点
            3. 获得一些个人发展建议
            """;
    }
    
    /**
     * 用户自我QA的分析提示词
     */
    public static String buildUserSelfQaAnalysisPrompt(String question, String userDescription) {
        return String.format("""
            【任务】分析是否需要补充信息
            
            【用户问题】%s
            
            【用户自我描述】%s
            
            请分析：
            1. 这个问题是否需要更多用户信息才能回答？
            2. 如果需要，需要补充什么信息？
            
            返回纯JSON格式（不要使用markdown代码块）：
            {
                "needsMoreInfo": true/false,
                "missingInfo": "需要补充的信息描述（如果需要）",
                "followUpQuestion": "如果需要补充信息，生成一个友好的追问"
            }
            
            如果不需要补充信息，返回：
            {
                "needsMoreInfo": false,
                "missingInfo": null,
                "followUpQuestion": null
            }
            
            【重要格式要求】
            - 只返回纯JSON格式，不要使用markdown代码块
            - 不要包含```json```、```等markdown标记
            - 不要添加任何解释文字
            - 直接返回JSON对象，从{开始到}结束
            
            只返回JSON，不要其他内容。
            """,
            question,
            userDescription != null ? userDescription : "（暂无自我描述）"
        );
    }
    
    /**
     * 用户自我QA的回答生成提示词
     * @deprecated 使用 buildBaseUserSelfQaSystemPrompt + 原生messages数组代替
     */
    @Deprecated
    public static String buildUserSelfQaAnswerPrompt(String question, String userDescription) {
        return String.format("""
            【任务】基于用户描述回答问题
            
            【用户问题】%s
            
            【用户自我描述】%s
            
            【回答要求】
            1. 基于用户提供的信息进行回答
            2. 如果信息不足，诚实地说明
            3. 可以提供一些反思性的问题帮助用户思考
            4. 语气要友好、支持性
            5. 可以基于已有信息进行合理的分析和建议
            
            只返回回答内容，不要其他说明。
            """,
            question,
            userDescription != null ? userDescription : "（暂无自我描述）"
        );
    }
    
    /**
     * 用户自我收集的最低信息提示词
     */
    public static String buildMinimumInfoQuestionForUser(Map<String, Object> data, List<String> missingInfo) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("用户想结束问卷，但还需要补充一些关键信息。\n\n");
        prompt.append("已收集信息：").append(formatCollectedData(data)).append("\n");
        prompt.append("缺失信息：").append(String.join(", ", missingInfo)).append("\n\n");
        prompt.append("请生成一个友好的问题，请求用户补充最重要的1条信息。\n");
        prompt.append("优先级：\n");
        if (missingInfo.contains("age")) { prompt.append("1. 年龄（你现在多大？）\n"); }
        if (missingInfo.contains("occupation")) { prompt.append("2. 职业（你做什么工作？）\n"); }
        if (missingInfo.contains("education")) { prompt.append("3. 教育背景（你的教育背景如何？）\n"); }
        if (missingInfo.contains("personality_characteristics")) { prompt.append("4. 性格特点（你觉得自己的性格如何？）\n"); }
        prompt.append("\n要求：\n1. 语气要理解用户想结束的心情\n2. 说明只需要再回答1个问题\n3. 问最重要的缺失信息\n4. 简短、友好\n5. 使用第二人称'你'\n\n只返回问题本身。");
        return prompt.toString();
    }
    
    /**
     * 构建将补充信息整合到描述中的Prompt
     */
    public static String buildIntegrateSupplementToDescriptionPrompt(
        String originalDescription,
        String supplementQuestion,
        String supplementInfo
    ) {
        return String.format("""
            【任务】将补充信息自然地整合到现有描述中
            
            【原始描述】
            %s
            
            【AI的补充问题】
            %s
            
            【用户补充的信息】
            %s
            
            【整合要求】
            1. 将补充信息自然地融入到原始描述中
            2. 如果补充信息与现有信息冲突，以补充信息为准（更新覆盖）
            3. 如果补充信息是新信息，添加到描述的合适位置
            4. 保持描述的流畅性和自然性
            5. 不要改变原始描述中其他未涉及的内容
            6. 使用第三人称描述（对于联系人）
            7. 不要添加用户没有明确提供的信息
            8. 保持原有的描述风格和语气
            9. 描述要完整、连贯，像是一段完整的文字
            
            【禁止行为】
            - 不要编造或推测额外信息
            - 不要删除原始描述中的有效信息
            - 不要改变描述的整体结构
            - 不要添加主观评价或判断
            - 不要使用"根据补充信息"等提示性语言
            - 不要分点列举，要写成连贯的段落
            
            【输出格式】
            只返回更新后的完整描述文本，不要其他说明或解释。
            """,
            originalDescription != null && !originalDescription.trim().isEmpty() 
                ? originalDescription 
                : "（暂无描述）",
            supplementQuestion,
            supplementInfo
        );
    }
    
    /**
     * 构建将补充信息整合到用户自我描述中的Prompt
     */
    public static String buildIntegrateSupplementToUserDescriptionPrompt(
        String originalDescription,
        String supplementQuestion,
        String supplementInfo
    ) {
        return String.format("""
            【任务】将补充信息自然地整合到现有的自我描述中
            
            【原始自我描述】
            %s
            
            【AI的补充问题】
            %s
            
            【用户补充的信息】
            %s
            
            【整合要求】
            1. 将补充信息自然地融入到原始描述中
            2. 如果补充信息与现有信息冲突，以补充信息为准（更新覆盖）
            3. 如果补充信息是新信息，添加到描述的合适位置
            4. 保持描述的流畅性和自然性
            5. 不要改变原始描述中其他未涉及的内容
            6. 使用第一人称描述（"我"）
            7. 不要添加用户没有明确提供的信息
            8. 保持原有的描述风格和语气
            9. 描述要完整、连贯，像是一段完整的自我介绍
            
            【禁止行为】
            - 不要编造或推测额外信息
            - 不要删除原始描述中的有效信息
            - 不要改变描述的整体结构
            - 不要添加主观评价或判断
            - 不要使用"根据补充信息"等提示性语言
            - 不要分点列举，要写成连贯的段落
            
            【输出格式】
            只返回更新后的完整自我描述文本，不要其他说明或解释。
            """,
            originalDescription != null && !originalDescription.trim().isEmpty() 
                ? originalDescription 
                : "（暂无描述）",
            supplementQuestion,
            supplementInfo
        );
    }
}


