package com.ulog.backend.conversation.eventlistener;

import com.ulog.backend.ai.RelationshipAnalysisService;
import com.ulog.backend.conversation.event.ContactCreatedEvent;
import com.ulog.backend.conversation.event.ContactDescriptionUpdatedEvent;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 监听联系人创建事件，异步生成关系分析和交往建议
 */
@Component
public class RelationshipAnalysisEventListener {

    private static final Logger log = LoggerFactory.getLogger(RelationshipAnalysisEventListener.class);

    private final RelationshipAnalysisService relationshipAnalysisService;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;

    public RelationshipAnalysisEventListener(
            RelationshipAnalysisService relationshipAnalysisService,
            ContactRepository contactRepository,
            UserRepository userRepository) {
        this.relationshipAnalysisService = relationshipAnalysisService;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
    }

    /**
     * 监听联系人创建事件
     * 生成：交往建议 + 关系分析（如果有用户描述）
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("selfValueTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleContactCreated(ContactCreatedEvent event) {
        Long contactId = event.getContactId();
        log.info("Handling ContactCreatedEvent for contact {} - generating relationship analysis", contactId);

        try {
            // 获取联系人
            var contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isEmpty()) {
                log.warn("Contact {} not found, skipping relationship analysis", contactId);
                return;
            }
            
            var contact = contactOpt.get();
            Long userId = contact.getOwner().getId();

            // Step 1: 生成交往建议（不依赖用户信息，总是生成）
            generateInteractionSuggestions(contactId, contact);

            // Step 2: 生成关系分析（需要用户有描述）
            if (hasUserDescription(userId)) {
                generateRelationshipAnalysis(contactId, userId, contact);
            } else {
                log.info("User {} has no description, skipping relationship analysis for contact {}", 
                        userId, contactId);
            }

            log.info("Successfully completed relationship analysis processing for contact {}", contactId);

        } catch (Exception e) {
            log.error("Failed to process relationship analysis for contact {}: {}", 
                    contactId, e.getMessage(), e);
        }
    }

    /**
     * 监听联系人描述更新事件
     * 重新生成：交往建议 + 关系分析（如果有用户描述）
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("selfValueTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleContactDescriptionUpdated(ContactDescriptionUpdatedEvent event) {
        Long contactId = event.getContactId();
        log.info("Handling ContactDescriptionUpdatedEvent for contact {} - regenerating relationship analysis", 
                contactId);

        try {
            // 获取联系人
            var contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isEmpty()) {
                log.warn("Contact {} not found, skipping relationship analysis regeneration", contactId);
                return;
            }

            var contact = contactOpt.get();
            Long userId = contact.getOwner().getId();

            // Step 1: 重新生成交往建议
            generateInteractionSuggestions(contactId, contact);

            // Step 2: 重新生成关系分析
            if (hasUserDescription(userId)) {
                generateRelationshipAnalysis(contactId, userId, contact);
            } else {
                log.info("User {} has no description, skipping relationship analysis regeneration for contact {}", 
                        userId, contactId);
                // 清空之前的关系分析
                contact.setAiSummary(null);
                contactRepository.save(contact);
            }

            log.info("Successfully regenerated relationship analysis for contact {}", contactId);

        } catch (Exception e) {
            log.error("Failed to regenerate relationship analysis for contact {}: {}", 
                    contactId, e.getMessage(), e);
        }
    }

    /**
     * 生成交往建议
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void generateInteractionSuggestions(Long contactId, com.ulog.backend.domain.contact.Contact contact) {
        try {
            log.info("Generating interaction suggestions for contact {}", contactId);
            
            String suggestions = relationshipAnalysisService.generateInteractionSuggestions(contactId)
                    .block();

            if (suggestions != null && !suggestions.trim().isEmpty()) {
                // 重新获取联系人实体，确保在独立事务中
                var contactOpt = contactRepository.findById(contactId);
                if (contactOpt.isPresent()) {
                    var contactToUpdate = contactOpt.get();
                    contactToUpdate.setInteractionSuggestions(suggestions);
                    contactRepository.save(contactToUpdate);
                    log.info("Successfully generated and saved interaction suggestions for contact {}", contactId);
                } else {
                    log.warn("Contact {} not found when updating interaction suggestions", contactId);
                }
            } else {
                log.warn("Empty interaction suggestions generated for contact {}", contactId);
                // 设置默认值
                setDefaultInteractionSuggestions(contactId);
            }

        } catch (Exception e) {
            log.error("Failed to generate interaction suggestions for contact {}: {}", 
                    contactId, e.getMessage(), e);
            // 设置默认值作为降级处理
            setDefaultInteractionSuggestions(contactId);
        }
    }

    /**
     * 生成关系分析
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void generateRelationshipAnalysis(Long contactId, Long userId, 
                                            com.ulog.backend.domain.contact.Contact contact) {
        try {
            log.info("Generating relationship analysis for contact {} and user {}", contactId, userId);
            
            String analysis = relationshipAnalysisService.generateRelationshipAnalysis(contactId, userId)
                    .block();

            if (analysis != null && !analysis.trim().isEmpty()) {
                // 重新获取联系人实体，确保在独立事务中
                var contactOpt = contactRepository.findById(contactId);
                if (contactOpt.isPresent()) {
                    var contactToUpdate = contactOpt.get();
                    contactToUpdate.setAiSummary(analysis);
                    contactRepository.save(contactToUpdate);
                    log.info("Successfully generated and saved relationship analysis for contact {}", contactId);
                } else {
                    log.warn("Contact {} not found when updating relationship analysis", contactId);
                }
            } else {
                log.warn("Empty relationship analysis generated for contact {}", contactId);
            }

        } catch (Exception e) {
            log.error("Failed to generate relationship analysis for contact {}: {}", 
                    contactId, e.getMessage(), e);
        }
    }

    /**
     * 检查用户是否有描述信息
     */
    private boolean hasUserDescription(Long userId) {
        return userRepository.findById(userId)
                .map(user -> user.getDescription() != null && !user.getDescription().trim().isEmpty())
                .orElse(false);
    }

    /**
     * 设置默认的交往建议
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void setDefaultInteractionSuggestions(Long contactId) {
        try {
            var contactOpt = contactRepository.findById(contactId);
            if (contactOpt.isPresent()) {
                var contactToUpdate = contactOpt.get();
                String defaultSuggestions = "### 1. 行为倾向推断\n" +
                        "基于现有信息，建议保持开放和尊重的态度进行交往。\n\n" +
                        "### 2. 情绪与价值观暗示\n" +
                        "建议关注对方的情绪变化，尊重其价值观和选择。\n\n" +
                        "### 3. 潜在需求推理\n" +
                        "保持真诚的沟通，了解对方的真实需求。\n\n" +
                        "### 4. 可能的禁忌雷区\n" +
                        "避免过于私人的话题，尊重对方的边界。\n\n" +
                        "### 5. 人际关系机会点\n" +
                        "通过共同兴趣和活动增进了解。\n\n" +
                        "### 6. 快乐清单\n" +
                        "保持积极正面的交流，分享有趣的话题。\n\n" +
                        "### 7. 雷点清单\n" +
                        "避免批评和负面情绪的表达。\n\n" +
                        "### 8. 关键时间点\n" +
                        "信息不足，建议在交往过程中了解重要日期。";
                
                contactToUpdate.setInteractionSuggestions(defaultSuggestions);
                contactRepository.save(contactToUpdate);
                log.info("Set default interaction suggestions for contact {}", contactId);
            }
        } catch (Exception e) {
            log.error("Failed to set default interaction suggestions for contact {}: {}", 
                    contactId, e.getMessage(), e);
        }
    }
}