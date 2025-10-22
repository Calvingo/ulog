package com.ulog.backend.contact.service;

import com.ulog.backend.ai.AiSummaryService;
import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.contact.dto.ContactRequest;
import com.ulog.backend.contact.dto.ContactResponse;
import com.ulog.backend.contact.dto.ContactUpdateRequest;
import com.ulog.backend.conversation.event.ContactCreatedEvent;
import com.ulog.backend.conversation.event.ContactDescriptionUpdatedEvent;
import com.ulog.backend.compliance.service.OperationLogService;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final AiSummaryService aiSummaryService;
    private final ApplicationEventPublisher eventPublisher;
    private final OperationLogService operationLogService;

    public ContactService(ContactRepository contactRepository, UserRepository userRepository, AiSummaryService aiSummaryService, ApplicationEventPublisher eventPublisher, OperationLogService operationLogService) {
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.aiSummaryService = aiSummaryService;
        this.eventPublisher = eventPublisher;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public ContactResponse create(Long userId, ContactRequest request) {
        User owner = loadUser(userId);
        Contact contact = new Contact(owner, request.getName(), request.getDescription());
        contactRepository.save(contact);
        
        // 记录联系人创建日志
        operationLogService.logOperation(userId, "contact_create", 
            String.format("Created contact: %s (name: %s)", contact.getId(), contact.getName()));
        
        // 🔥 发布事件：触发 self value 计算（基于description）
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            log.debug("Publishing ContactCreatedEvent for contact {}", contact.getId());
            eventPublisher.publishEvent(new ContactCreatedEvent(contact.getId(), request.getDescription()));
        }
        
        return map(contact);
    }

    @Transactional(readOnly = true)
    public List<ContactResponse> list(Long userId) {
        User owner = loadUser(userId);
        return contactRepository.findAllByOwnerAndDeletedFalseOrderByCreatedAtDesc(owner).stream()
            .map(this::map)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContactResponse get(Long userId, Long contactId) {
        Contact contact = findOwnedContact(userId, contactId);
        return map(contact);
    }

    @Transactional
    public ContactResponse update(Long userId, Long contactId, ContactUpdateRequest request) {
        Contact contact = findOwnedContact(userId, contactId);
        if (request.getName() == null && request.getDescription() == null && request.getAiSummary() == null) {
            throw new BadRequestException("no fields to update");
        }
        if (request.getName() != null) {
            contact.setName(request.getName());
        }
        if (request.getDescription() != null) {
            contact.setDescription(request.getDescription());
            // 🔥 发布事件：触发 self value 重新计算（基于新的description）
            log.debug("Publishing ContactDescriptionUpdatedEvent for contact {}", contactId);
            eventPublisher.publishEvent(new ContactDescriptionUpdatedEvent(contactId, request.getDescription()));
        }
        if (request.getAiSummary() != null) {
            contact.setAiSummary(request.getAiSummary());
        } else if (request.getDescription() != null) {
            populateSummaryIfNeeded(contact, request.getDescription(), null);
        }
        
        // 记录联系人更新日志
        operationLogService.logOperation(userId, "contact_update", 
            String.format("Updated contact: %s (name: %s)", contact.getId(), contact.getName()));
        
        return map(contact);
    }

    @Transactional
    public void delete(Long userId, Long contactId) {
        Contact contact = findOwnedContact(userId, contactId);
        contact.setDeleted(true);
        
        // 记录联系人删除日志
        operationLogService.logOperation(userId, "contact_delete", 
            String.format("Deleted contact: %s (name: %s)", contact.getId(), contact.getName()));
    }

    private Contact findOwnedContact(Long userId, Long contactId) {
        User owner = loadUser(userId);
        return contactRepository.findByIdAndOwnerAndDeletedFalse(contactId, owner)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "contact not found"));
    }

    private User loadUser(Long userId) {
        return userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
    }

    private ContactResponse map(Contact contact) {
        return new ContactResponse(contact.getId(), contact.getName(), contact.getDescription(), contact.getAiSummary(), contact.getCreatedAt(), contact.getUpdatedAt());
    }

    private void populateSummaryIfNeeded(Contact contact, String description, String requestedSummary) {
        if (requestedSummary != null && !requestedSummary.isBlank()) {
            contact.setAiSummary(requestedSummary);
            return;
        }
        try {
            String generated = aiSummaryService.generateAiSummary(description);
            if (generated != null && !generated.isBlank()) {
                contact.setAiSummary(generated);
            }
        } catch (Exception ex) {
            log.warn("AI summary generation failed for contact {}: {}", contact.getId(), ex.getMessage());
            log.debug("AI summary generation error", ex);
        }
    }
}
