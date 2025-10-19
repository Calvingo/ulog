package com.ulog.backend.push.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.goal.dto.RegisterPushTokenRequest;
import com.ulog.backend.push.PushTokenService;
import com.ulog.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/push/tokens")
@Tag(name = "Push Tokens", description = "推送令牌管理接口")
@SecurityRequirement(name = "Bearer Authentication")
public class PushTokenController {

    private final PushTokenService pushTokenService;

    public PushTokenController(PushTokenService pushTokenService) {
        this.pushTokenService = pushTokenService;
    }

    @PostMapping
    @Operation(summary = "注册推送令牌", description = "注册设备的推送通知令牌")
    public ApiResponse<Void> registerToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody RegisterPushTokenRequest request) {
        pushTokenService.registerToken(principal.getUserId(), request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{tokenId}")
    @Operation(summary = "注销推送令牌", description = "停用指定的推送令牌")
    public ApiResponse<Void> deactivateToken(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long tokenId) {
        pushTokenService.deactivateToken(principal.getUserId(), tokenId);
        return ApiResponse.success(null);
    }
}

