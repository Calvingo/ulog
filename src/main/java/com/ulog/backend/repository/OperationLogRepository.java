package com.ulog.backend.repository;

import com.ulog.backend.domain.compliance.OperationLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<OperationLog> findByOperationTypeOrderByCreatedAtDesc(String operationType);

    @Modifying
    @Query("DELETE FROM OperationLog ol WHERE ol.createdAt < :cutoffDate")
    int deleteOldLogs(@Param("cutoffDate") LocalDateTime cutoffDate);
}

