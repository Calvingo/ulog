package com.ulog.backend.compliance.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.compliance.annotation.LogOperation;
import com.ulog.backend.compliance.dto.PrivacyConsentRequest;
import com.ulog.backend.compliance.dto.PrivacyConsentResponse;
import com.ulog.backend.compliance.service.PrivacyConsentService;
import com.ulog.backend.domain.compliance.UserPrivacyConsent;
import com.ulog.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
@Tag(name = "Privacy", description = "隐私政策相关API")
public class PrivacyController {

    private final PrivacyConsentService privacyConsentService;

    public PrivacyController(PrivacyConsentService privacyConsentService) {
        this.privacyConsentService = privacyConsentService;
    }

    @GetMapping("/policy")
    @Operation(summary = "获取隐私政策信息", description = "返回当前隐私政策版本和URL")
    public ApiResponse<Map<String, Object>> getPrivacyPolicy() {
        Map<String, Object> policy = new HashMap<>();
        policy.put("version", privacyConsentService.getCurrentPolicyVersion());
        policy.put("url", privacyConsentService.getPolicyUrl());
        policy.put("required", privacyConsentService.isPolicyConsentRequired());
        return ApiResponse.success(policy);
    }

    @GetMapping("/consent/status")
    @Operation(summary = "检查用户隐私政策同意状态", description = "检查用户是否已同意当前版本的隐私政策")
    public ApiResponse<Map<String, Object>> getConsentStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        boolean hasConsented = privacyConsentService.hasConsentedToCurrentPolicy(userPrincipal.getUserId());
        
        Map<String, Object> status = new HashMap<>();
        status.put("hasConsented", hasConsented);
        status.put("currentVersion", privacyConsentService.getCurrentPolicyVersion());
        status.put("required", privacyConsentService.isPolicyConsentRequired());
        
        return ApiResponse.success(status);
    }

    @PostMapping("/consent")
    @LogOperation(value = "privacy_consent", description = "用户同意隐私政策")
    @Operation(summary = "记录隐私政策同意", description = "记录用户对隐私政策的同意")
    public ApiResponse<PrivacyConsentResponse> recordConsent(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody PrivacyConsentRequest request,
            HttpServletRequest httpRequest) {
        
        if (!request.isAccepted()) {
            return ApiResponse.error(com.ulog.backend.common.api.ErrorCode.BAD_REQUEST, "必须同意隐私政策才能使用本应用");
        }

        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        UserPrivacyConsent consent = privacyConsentService.recordConsent(
            userPrincipal.getUserId(), 
            request.getPolicyVersion(), 
            ipAddress, 
            userAgent
        );

        PrivacyConsentResponse response = new PrivacyConsentResponse(
            consent.getId(),
            consent.getUserId(),
            consent.getPolicyVersion(),
            consent.getConsentTime(),
            true
        );

        return ApiResponse.success(response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}

