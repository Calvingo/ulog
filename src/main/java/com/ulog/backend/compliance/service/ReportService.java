package com.ulog.backend.compliance.service;

import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.compliance.dto.ReportRequest;
import com.ulog.backend.compliance.dto.ReportResponse;
import com.ulog.backend.domain.compliance.UserReport;
import com.ulog.backend.repository.UserReportRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final UserReportRepository reportRepository;

    public ReportService(UserReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    /**
     * 提交举报
     */
    @Transactional
    public ReportResponse submitReport(Long reporterId, ReportRequest request) {
        log.info("User {} submitting report of type {}", reporterId, request.getReportType());

        // 验证举报类型
        validateReportType(request.getReportType());

        UserReport report = new UserReport();
        report.setReporterId(reporterId);
        report.setReportedUserId(request.getReportedUserId());
        report.setReportType(request.getReportType());
        report.setReportCategory(request.getReportCategory());
        report.setContent(request.getContent());
        report.setEvidence(request.getEvidence());
        report.setStatus("pending");

        UserReport saved = reportRepository.save(report);
        
        // TODO: 发送通知给管理员
        notifyAdmins(saved);

        return toResponse(saved);
    }

    /**
     * 获取用户的举报记录
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getUserReports(Long userId) {
        List<UserReport> reports = reportRepository.findByReporterIdOrderByCreatedAtDesc(userId);
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 获取针对某用户的举报
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getReportsAgainstUser(Long userId) {
        List<UserReport> reports = reportRepository.findByReportedUserIdOrderByCreatedAtDesc(userId);
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 获取所有待处理的举报（管理员用）
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getPendingReports() {
        List<UserReport> reports = reportRepository.findByStatusOrderByCreatedAtDesc("pending");
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 获取所有举报（管理员用）
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getAllReports() {
        List<UserReport> reports = reportRepository.findByOrderByCreatedAtDesc();
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * 更新举报状态（管理员用）
     */
    @Transactional
    public ReportResponse updateReportStatus(Long reportId, String status, String adminNotes, Long adminId) {
        UserReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new BadRequestException("Report not found"));

        report.setStatus(status);
        report.setAdminNotes(adminNotes);
        report.setProcessedBy(adminId);
        report.setProcessedAt(LocalDateTime.now());

        UserReport updated = reportRepository.save(report);
        log.info("Report {} status updated to {} by admin {}", reportId, status, adminId);

        return toResponse(updated);
    }

    /**
     * 验证举报类型
     */
    private void validateReportType(String reportType) {
        List<String> validTypes = List.of(
            "inappropriate_content", "violation", "harassment", "spam", "other"
        );
        
        if (!validTypes.contains(reportType)) {
            throw new BadRequestException("Invalid report type: " + reportType);
        }
    }

    /**
     * 通知管理员（TODO: 实现邮件或其他通知方式）
     */
    private void notifyAdmins(UserReport report) {
        log.info("New report #{} submitted, admin notification needed", report.getId());
        // TODO: 实现邮件通知或其他通知机制
    }

    /**
     * 转换为响应DTO
     */
    private ReportResponse toResponse(UserReport report) {
        return new ReportResponse(
            report.getId(),
            report.getReporterId(),
            report.getReportedUserId(),
            report.getReportType(),
            report.getReportCategory(),
            report.getContent(),
            report.getStatus(),
            report.getCreatedAt(),
            report.getProcessedAt()
        );
    }
}

