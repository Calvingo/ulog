package com.ulog.backend.repository;

import com.ulog.backend.domain.compliance.UserReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<UserReport, Long> {

    List<UserReport> findByReporterIdOrderByCreatedAtDesc(Long reporterId);

    List<UserReport> findByTargetTypeOrderByCreatedAtDesc(String targetType);

    List<UserReport> findByTargetIdOrderByCreatedAtDesc(String targetId);

    List<UserReport> findByStatusOrderByCreatedAtDesc(String status);

    List<UserReport> findByOrderByCreatedAtDesc();
}

