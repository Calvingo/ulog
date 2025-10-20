package com.ulog.backend.repository;

import com.ulog.backend.domain.compliance.ContentModerationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentModerationLogRepository extends JpaRepository<ContentModerationLog, Long> {

    List<ContentModerationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<ContentModerationLog> findByModerationResultOrderByCreatedAtDesc(String moderationResult);

    @Modifying
    @Query("DELETE FROM ContentModerationLog cml WHERE cml.createdAt < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}

