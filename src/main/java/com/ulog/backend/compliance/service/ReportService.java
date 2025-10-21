package com.ulog.backend.compliance.service;

import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.compliance.dto.ReportRequest;
import com.ulog.backend.compliance.dto.ReportResponse;
import com.ulog.backend.compliance.enums.ReportStatus;
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
     * 提交AI内容反馈/系统问题报告
     */
    @Transactional
    public ReportResponse submitReport(Long reporterId, ReportRequest request) {
        log.info("User {} submitting report: type={}, target={}, targetId={}", 
            reporterId, request.getReportType(), request.getTargetType(), request.getTargetId());

        UserReport report = new UserReport();
        report.setReporterId(reporterId);
        report.setReportType(request.getReportType().name());
        report.setTargetType(request.getTargetType().name());
        report.setTargetId(request.getTargetId());
        report.setDescription(request.getDescription());
        report.setContext(request.getContext());
        report.setEvidenceUrls(request.getEvidenceUrls());
        report.setStatus(ReportStatus.PENDING.name());

        UserReport saved = reportRepository.save(report);
        
        // 记录日志供后续分析
        logReportForAnalysis(saved);

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
     * 获取所有待处理的举报（管理员用）
     */
    @Transactional(readOnly = true)
    public List<ReportResponse> getPendingReports() {
        List<UserReport> reports = reportRepository.findByStatusOrderByCreatedAtDesc(
            ReportStatus.PENDING.name());
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
    public ReportResponse updateReportStatus(Long reportId, ReportStatus status, 
                                            String adminNotes, Long adminId) {
        UserReport report = reportRepository.findById(reportId)
            .orElseThrow(() -> new BadRequestException("Report not found"));

        report.setStatus(status.name());
        report.setAdminNotes(adminNotes);
        report.setProcessedBy(adminId);
        report.setProcessedAt(LocalDateTime.now());

        UserReport updated = reportRepository.save(report);
        log.info("Report {} status updated to {} by admin {}", reportId, status, adminId);

        return toResponse(updated);
    }

    /**
     * 记录举报信息用于分析
     * 这些数据可以帮助：
     * 1. 发现AI Prompt问题
     * 2. 识别系统Bug
     * 3. 收集产品改进建议
     */
    private void logReportForAnalysis(UserReport report) {
        log.info("📊 Report Analysis Data: id={}, type={}, target={}, targetId={}", 
            report.getId(), report.getReportType(), report.getTargetType(), report.getTargetId());
        
        // 高优先级问题立即记录
        if ("AI_INAPPROPRIATE_CONTENT".equals(report.getReportType())) {
            log.warn("⚠️ CRITICAL: Inappropriate AI content reported! id={}, context={}", 
                report.getId(), report.getContext());
        }
    }

    /**
     * 转换为响应DTO
     */
    private ReportResponse toResponse(UserReport report) {
        ReportResponse response = new ReportResponse();
        response.setId(report.getId());
        response.setReporterId(report.getReporterId());
        response.setReportType(
            report.getReportType() != null 
                ? com.ulog.backend.compliance.enums.ReportType.valueOf(report.getReportType()) 
                : null
        );
        response.setTargetType(
            report.getTargetType() != null 
                ? com.ulog.backend.compliance.enums.ReportTarget.valueOf(report.getTargetType()) 
                : null
        );
        response.setTargetId(report.getTargetId());
        response.setDescription(report.getDescription());
        response.setContext(report.getContext());
        response.setEvidenceUrls(report.getEvidenceUrls());
        response.setStatus(
            report.getStatus() != null 
                ? ReportStatus.valueOf(report.getStatus()) 
                : null
        );
        response.setAdminNotes(report.getAdminNotes());
        response.setCreatedAt(report.getCreatedAt());
        response.setProcessedAt(report.getProcessedAt());
        return response;
    }
}
