package com.ulog.backend.compliance.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.compliance.annotation.LogOperation;
import com.ulog.backend.compliance.dto.ReportRequest;
import com.ulog.backend.compliance.dto.ReportResponse;
import com.ulog.backend.compliance.service.ReportService;
import com.ulog.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "用户举报相关API")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @LogOperation(value = "submit_report", description = "提交用户举报")
    @Operation(summary = "提交举报", description = "提交对用户或内容的举报")
    public ApiResponse<ReportResponse> submitReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ReportRequest request) {
        
        ReportResponse response = reportService.submitReport(userPrincipal.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping("/my-reports")
    @Operation(summary = "查看我的举报", description = "查看当前用户提交的所有举报")
    public ApiResponse<List<ReportResponse>> getMyReports(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        List<ReportResponse> reports = reportService.getUserReports(userPrincipal.getUserId());
        return ApiResponse.success(reports);
    }

    // 管理员功能 - 后续可以添加管理员角色验证
    @GetMapping("/pending")
    @Operation(summary = "获取待处理举报（管理员）", description = "获取所有待处理的举报")
    public ApiResponse<List<ReportResponse>> getPendingReports() {
        List<ReportResponse> reports = reportService.getPendingReports();
        return ApiResponse.success(reports);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有举报（管理员）", description = "获取所有举报记录")
    public ApiResponse<List<ReportResponse>> getAllReports() {
        List<ReportResponse> reports = reportService.getAllReports();
        return ApiResponse.success(reports);
    }
}

