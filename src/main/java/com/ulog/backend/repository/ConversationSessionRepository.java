package com.ulog.backend.repository;

import com.ulog.backend.domain.conversation.ConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationSessionRepository extends JpaRepository<ConversationSession, String> {
    
    /**
     * 查找用户的所有会话
     */
    List<ConversationSession> findByUserId(Long userId);
    
    /**
     * 查找用户的活跃会话
     */
    List<ConversationSession> findByUserIdAndStatus(Long userId, String status);
    
    /**
     * 查找用户和会话ID
     */
    Optional<ConversationSession> findBySessionIdAndUserId(String sessionId, Long userId);
    
    /**
     * 查找过期的会话（用于定时清理）
     */
    List<ConversationSession> findByLastActiveAtBeforeAndStatusIn(
        LocalDateTime before, 
        List<String> statuses
    );
    
    /**
     * 根据联系人ID查找会话
     */
    Optional<ConversationSession> findByContactId(Long contactId);
}

