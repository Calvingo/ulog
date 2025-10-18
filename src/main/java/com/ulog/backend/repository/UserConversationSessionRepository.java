package com.ulog.backend.repository;

import com.ulog.backend.conversation.enums.SessionStatus;
import com.ulog.backend.domain.conversation.UserConversationSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConversationSessionRepository extends JpaRepository<UserConversationSession, String> {
    
    /**
     * 查找用户的活跃会话（信息收集或QA）
     */
    Optional<UserConversationSession> findByUserIdAndStatus(Long userId, SessionStatus status);
    
    /**
     * 查找用户的QA会话（COMPLETED或QA_ACTIVE）
     */
    Optional<UserConversationSession> findByUserIdAndStatusIn(Long userId, List<SessionStatus> statuses);
    
    /**
     * 获取用户的历史会话
     */
    List<UserConversationSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    /**
     * 检查用户是否有活跃会话
     */
    boolean existsByUserIdAndStatusIn(Long userId, List<SessionStatus> statuses);
}

