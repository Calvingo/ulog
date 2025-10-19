package com.ulog.backend.pin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.conversation.dto.QaHistoryEntry;
import com.ulog.backend.conversation.service.QaHistoryService;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.conversation.ConversationSession;
import com.ulog.backend.domain.conversation.UserConversationSession;
import com.ulog.backend.domain.pin.Pin;
import com.ulog.backend.domain.pin.PinSourceType;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.pin.dto.CreatePinRequest;
import com.ulog.backend.pin.dto.PinResponse;
import com.ulog.backend.pin.dto.PinSummaryResponse;
import com.ulog.backend.pin.dto.UpdatePinRequest;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.ConversationSessionRepository;
import com.ulog.backend.repository.PinRepository;
import com.ulog.backend.repository.UserConversationSessionRepository;
import com.ulog.backend.repository.UserRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PinService {

    private static final Logger log = LoggerFactory.getLogger(PinService.class);

    private final PinRepository pinRepository;
    private final ConversationSessionRepository conversationSessionRepository;
    private final UserConversationSessionRepository userConversationSessionRepository;
    private final QaHistoryService qaHistoryService;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public PinService(PinRepository pinRepository,
                     ConversationSessionRepository conversationSessionRepository,
                     UserConversationSessionRepository userConversationSessionRepository,
                     QaHistoryService qaHistoryService,
                     ContactRepository contactRepository,
                     UserRepository userRepository,
                     ObjectMapper objectMapper) {
        this.pinRepository = pinRepository;
        this.conversationSessionRepository = conversationSessionRepository;
        this.userConversationSessionRepository = userConversationSessionRepository;
        this.qaHistoryService = qaHistoryService;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PinResponse createPin(Long userId, CreatePinRequest request) {
        // 检查是否已经Pin过
        if (pinRepository.existsByUserIdAndSessionIdAndQaIndex(userId, request.getSessionId(), request.getQaIndex())) {
            throw new BadRequestException("This QA has already been pinned");
        }

        // 尝试从联系人会话加载
        ConversationSession contactSession = conversationSessionRepository
            .findById(request.getSessionId()).orElse(null);

        if (contactSession != null) {
            // 验证所有权
            if (!contactSession.getUserId().equals(userId)) {
                throw new ApiException(ErrorCode.FORBIDDEN, "Cannot pin another user's conversation");
            }
            return createPinFromContactSession(userId, contactSession, request);
        }

        // 尝试从用户会话加载
        UserConversationSession userSession = userConversationSessionRepository
            .findById(request.getSessionId()).orElse(null);

        if (userSession != null) {
            // 验证所有权
            if (!userSession.getUserId().equals(userId)) {
                throw new ApiException(ErrorCode.FORBIDDEN, "Cannot pin another user's conversation");
            }
            return createPinFromUserSession(userId, userSession, request);
        }

        throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Session not found");
    }

    @Transactional(readOnly = true)
    public List<PinSummaryResponse> listPins(Long userId, Long contactId, String sourceTypeStr) {
        List<Pin> pins;

        if (contactId != null && sourceTypeStr != null) {
            // 按联系人和类型筛选
            PinSourceType sourceType = PinSourceType.valueOf(sourceTypeStr);
            pins = pinRepository.findAllByUserIdAndContactIdOrderByCreatedAtDesc(userId, contactId)
                .stream()
                .filter(p -> p.getSourceType() == sourceType)
                .collect(Collectors.toList());
        } else if (contactId != null) {
            // 仅按联系人筛选
            pins = pinRepository.findAllByUserIdAndContactIdOrderByCreatedAtDesc(userId, contactId);
        } else if (sourceTypeStr != null) {
            // 仅按类型筛选
            PinSourceType sourceType = PinSourceType.valueOf(sourceTypeStr);
            pins = pinRepository.findAllByUserIdAndSourceTypeOrderByCreatedAtDesc(userId, sourceType);
        } else {
            // 获取所有
            pins = pinRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        }

        return pins.stream()
            .map(this::mapToSummaryResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PinResponse getPin(Long userId, Long pinId) {
        Pin pin = pinRepository.findByIdAndUserId(pinId, userId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pin not found"));

        return mapToResponse(pin);
    }

    @Transactional
    public PinResponse updatePin(Long userId, Long pinId, UpdatePinRequest request) {
        Pin pin = pinRepository.findByIdAndUserId(pinId, userId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pin not found"));

        if (request.getNote() == null && request.getTags() == null) {
            throw new BadRequestException("No fields to update");
        }

        if (request.getNote() != null) {
            pin.setNote(request.getNote());
        }

        if (request.getTags() != null) {
            pin.setTags(request.getTags());
        }

        pinRepository.save(pin);
        log.info("Updated pin {} for user {}", pinId, userId);

        return mapToResponse(pin);
    }

    @Transactional
    public void deletePin(Long userId, Long pinId) {
        Pin pin = pinRepository.findByIdAndUserId(pinId, userId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Pin not found"));

        pinRepository.delete(pin);
        log.info("Deleted pin {} for user {}", pinId, userId);
    }

    @Transactional(readOnly = true)
    public boolean isPinned(Long userId, String sessionId, Integer qaIndex) {
        return pinRepository.existsByUserIdAndSessionIdAndQaIndex(userId, sessionId, qaIndex);
    }

    private PinResponse createPinFromContactSession(Long userId, ConversationSession session, 
                                                    CreatePinRequest request) {
        List<QaHistoryEntry> qaHistory = qaHistoryService.getContactQaHistory(session.getSessionId());

        if (request.getQaIndex() >= qaHistory.size()) {
            throw new BadRequestException("QA index out of range");
        }

        QaHistoryEntry qaEntry = qaHistory.get(request.getQaIndex());

        Pin pin = new Pin();
        pin.setUserId(userId);
        pin.setSourceType(PinSourceType.CONTACT_QA);
        pin.setSessionId(session.getSessionId());
        pin.setQaIndex(request.getQaIndex());
        pin.setContactId(session.getContactId());
        pin.setQuestion(qaEntry.getQuestion());
        pin.setAnswer(qaEntry.getAnswer());
        pin.setSupplementQuestion(qaEntry.getSupplementQuestion());
        pin.setSupplementAnswer(qaEntry.getSupplementAnswer());
        pin.setNeedsMoreInfo(qaEntry.getNeedsMoreInfo());
        pin.setQaTimestamp(qaEntry.getTimestamp());
        pin.setNote(request.getNote());
        pin.setTags(request.getTags());

        // 构建上下文信息
        String contextInfo = buildContextInfo(session);
        pin.setContextInfo(contextInfo);

        pinRepository.save(pin);
        log.info("Created pin from contact QA session {} index {} for user {}", 
                 session.getSessionId(), request.getQaIndex(), userId);

        return mapToResponse(pin);
    }

    private PinResponse createPinFromUserSession(Long userId, UserConversationSession session, 
                                                 CreatePinRequest request) {
        List<QaHistoryEntry> qaHistory = qaHistoryService.getUserQaHistory(session.getSessionId());

        if (request.getQaIndex() >= qaHistory.size()) {
            throw new BadRequestException("QA index out of range");
        }

        QaHistoryEntry qaEntry = qaHistory.get(request.getQaIndex());

        Pin pin = new Pin();
        pin.setUserId(userId);
        pin.setSourceType(PinSourceType.USER_QA);
        pin.setSessionId(session.getSessionId());
        pin.setQaIndex(request.getQaIndex());
        pin.setContactId(null);
        pin.setQuestion(qaEntry.getQuestion());
        pin.setAnswer(qaEntry.getAnswer());
        pin.setSupplementQuestion(qaEntry.getSupplementQuestion());
        pin.setSupplementAnswer(qaEntry.getSupplementAnswer());
        pin.setNeedsMoreInfo(qaEntry.getNeedsMoreInfo());
        pin.setQaTimestamp(qaEntry.getTimestamp());
        pin.setNote(request.getNote());
        pin.setTags(request.getTags());

        // 构建上下文信息
        String contextInfo = buildContextInfo(session);
        pin.setContextInfo(contextInfo);

        pinRepository.save(pin);
        log.info("Created pin from user QA session {} index {} for user {}", 
                 session.getSessionId(), request.getQaIndex(), userId);

        return mapToResponse(pin);
    }

    private String buildContextInfo(ConversationSession session) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("sessionType", "联系人QA");
            
            if (session.getContactId() != null) {
                Contact contact = contactRepository.findById(session.getContactId()).orElse(null);
                if (contact != null) {
                    context.put("contactName", contact.getName());
                }
            } else if (session.getContactName() != null) {
                context.put("contactName", session.getContactName());
            }
            
            context.put("sessionDate", session.getCreatedAt().toLocalDate().toString());
            
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            log.error("Failed to build context info: {}", e.getMessage());
            return "{}";
        }
    }

    private String buildContextInfo(UserConversationSession session) {
        try {
            Map<String, Object> context = new HashMap<>();
            context.put("sessionType", "用户自我QA");
            context.put("sessionDate", session.getCreatedAt().toLocalDate().toString());
            
            return objectMapper.writeValueAsString(context);
        } catch (Exception e) {
            log.error("Failed to build context info: {}", e.getMessage());
            return "{}";
        }
    }

    private PinResponse mapToResponse(Pin pin) {
        String contactName = extractContactName(pin);
        List<String> tagsList = parseTags(pin.getTags());

        return new PinResponse(
            pin.getId(),
            pin.getSourceType(),
            pin.getSessionId(),
            pin.getQaIndex(),
            pin.getContactId(),
            contactName,
            pin.getQuestion(),
            pin.getAnswer(),
            pin.getSupplementQuestion(),
            pin.getSupplementAnswer(),
            Boolean.TRUE.equals(pin.getNeedsMoreInfo()),
            pin.getNote(),
            tagsList,
            pin.getQaTimestamp(),
            pin.getCreatedAt(),
            pin.getUpdatedAt()
        );
    }

    private PinSummaryResponse mapToSummaryResponse(Pin pin) {
        String contactName = extractContactName(pin);
        List<String> tagsList = parseTags(pin.getTags());
        
        String questionPreview = truncate(pin.getQuestion(), 50);
        
        // 对于有补充信息的，使用最终回答；否则使用直接回答
        String finalAnswer = Boolean.TRUE.equals(pin.getNeedsMoreInfo()) && pin.getSupplementAnswer() != null
            ? pin.getSupplementAnswer()
            : pin.getAnswer();
        String answerPreview = truncate(finalAnswer, 100);

        return new PinSummaryResponse(
            pin.getId(),
            pin.getSourceType(),
            pin.getContactId(),
            contactName,
            questionPreview,
            answerPreview,
            Boolean.TRUE.equals(pin.getNeedsMoreInfo()),
            tagsList,
            pin.getCreatedAt()
        );
    }

    private String extractContactName(Pin pin) {
        if (pin.getContextInfo() == null) {
            return null;
        }

        try {
            Map<String, Object> context = objectMapper.readValue(pin.getContextInfo(), Map.class);
            return (String) context.get("contactName");
        } catch (Exception e) {
            log.debug("Failed to extract contact name from context: {}", e.getMessage());
            return null;
        }
    }

    private List<String> parseTags(String tagsStr) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return Arrays.stream(tagsStr.split(","))
            .map(String::trim)
            .filter(tag -> !tag.isEmpty())
            .collect(Collectors.toList());
    }

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }
}

