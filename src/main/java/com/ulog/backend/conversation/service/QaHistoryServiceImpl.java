package com.ulog.backend.conversation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ulog.backend.conversation.dto.QaHistoryEntry;
import com.ulog.backend.domain.conversation.ConversationSession;
import com.ulog.backend.domain.conversation.UserConversationSession;
import com.ulog.backend.repository.ConversationSessionRepository;
import com.ulog.backend.repository.UserConversationSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * QA对话历史服务实现类
 */
@Service
public class QaHistoryServiceImpl implements QaHistoryService {
    
    private static final Logger log = LoggerFactory.getLogger(QaHistoryServiceImpl.class);
    
    private final ConversationSessionRepository conversationSessionRepository;
    private final UserConversationSessionRepository userConversationSessionRepository;
    private final ObjectMapper objectMapper;
    
    public QaHistoryServiceImpl(
        ConversationSessionRepository conversationSessionRepository,
        UserConversationSessionRepository userConversationSessionRepository,
        ObjectMapper objectMapper
    ) {
        this.conversationSessionRepository = conversationSessionRepository;
        this.userConversationSessionRepository = userConversationSessionRepository;
        this.objectMapper = objectMapper;
    }
    
    @Override
    @Transactional
    public void addContactQaEntry(String sessionId, QaHistoryEntry entry) {
        try {
            ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Conversation session not found: " + sessionId));
            
            List<QaHistoryEntry> history = getContactQaHistory(sessionId);
            history.add(entry);
            
            String qaHistoryJson = objectMapper.writeValueAsString(history);
            session.setQaHistory(qaHistoryJson);
            conversationSessionRepository.save(session);
            
            log.info("Added QA entry to contact session {}: {}", sessionId, entry.getQuestion());
        } catch (JsonProcessingException e) {
            log.error("Failed to save QA history for contact session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void addUserQaEntry(String sessionId, QaHistoryEntry entry) {
        try {
            UserConversationSession session = userConversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("User conversation session not found: " + sessionId));
            
            List<QaHistoryEntry> history = getUserQaHistory(sessionId);
            history.add(entry);
            
            String qaHistoryJson = objectMapper.writeValueAsString(history);
            session.setQaHistory(qaHistoryJson);
            userConversationSessionRepository.save(session);
            
            log.info("Added QA entry to user session {}: {}", sessionId, entry.getQuestion());
        } catch (JsonProcessingException e) {
            log.error("Failed to save QA history for user session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QaHistoryEntry> getContactQaHistory(String sessionId) {
        try {
            ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Conversation session not found: " + sessionId));
            
            String qaHistoryJson = session.getQaHistory();
            if (qaHistoryJson == null || qaHistoryJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            return objectMapper.readValue(qaHistoryJson, new TypeReference<List<QaHistoryEntry>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse QA history for contact session {}: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<QaHistoryEntry> getUserQaHistory(String sessionId) {
        try {
            UserConversationSession session = userConversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("User conversation session not found: " + sessionId));
            
            String qaHistoryJson = session.getQaHistory();
            if (qaHistoryJson == null || qaHistoryJson.trim().isEmpty()) {
                return new ArrayList<>();
            }
            
            return objectMapper.readValue(qaHistoryJson, new TypeReference<List<QaHistoryEntry>>() {});
        } catch (JsonProcessingException e) {
            log.error("Failed to parse QA history for user session {}: {}", sessionId, e.getMessage(), e);
            return new ArrayList<>();
        }
    }
    
    @Override
    @Transactional
    public void updateLastContactQaEntry(String sessionId, QaHistoryEntry entry) {
        try {
            ConversationSession session = conversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Conversation session not found: " + sessionId));
            
            List<QaHistoryEntry> history = getContactQaHistory(sessionId);
            if (history.isEmpty()) {
                log.warn("No QA history to update for contact session {}", sessionId);
                return;
            }
            
            // 更新最后一条记录
            history.set(history.size() - 1, entry);
            
            String qaHistoryJson = objectMapper.writeValueAsString(history);
            session.setQaHistory(qaHistoryJson);
            conversationSessionRepository.save(session);
            
            log.info("Updated last QA entry for contact session {}", sessionId);
        } catch (JsonProcessingException e) {
            log.error("Failed to update QA history for contact session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional
    public void updateLastUserQaEntry(String sessionId, QaHistoryEntry entry) {
        try {
            UserConversationSession session = userConversationSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("User conversation session not found: " + sessionId));
            
            List<QaHistoryEntry> history = getUserQaHistory(sessionId);
            if (history.isEmpty()) {
                log.warn("No QA history to update for user session {}", sessionId);
                return;
            }
            
            // 更新最后一条记录
            history.set(history.size() - 1, entry);
            
            String qaHistoryJson = objectMapper.writeValueAsString(history);
            session.setQaHistory(qaHistoryJson);
            userConversationSessionRepository.save(session);
            
            log.info("Updated last QA entry for user session {}", sessionId);
        } catch (JsonProcessingException e) {
            log.error("Failed to update QA history for user session {}: {}", sessionId, e.getMessage(), e);
        }
    }
    
    @Override
    public String formatQaHistoryForPrompt(List<QaHistoryEntry> history) {
        if (history == null || history.isEmpty()) {
            return "（暂无历史对话）";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < history.size(); i++) {
            QaHistoryEntry entry = history.get(i);
            sb.append(String.format("第%d轮对话：\n", i + 1));
            sb.append(String.format("用户问题：%s\n", entry.getQuestion()));
            
            if (entry.getNeedsMoreInfo() != null && entry.getNeedsMoreInfo()) {
                if (entry.getSupplementQuestion() != null) {
                    sb.append(String.format("补充问题：%s\n", entry.getSupplementQuestion()));
                }
                if (entry.getSupplementAnswer() != null) {
                    sb.append(String.format("补充回答：%s\n", entry.getSupplementAnswer()));
                }
            } else {
                sb.append(String.format("AI回答：%s\n", entry.getAnswer()));
            }
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
