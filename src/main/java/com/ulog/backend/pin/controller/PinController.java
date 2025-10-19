package com.ulog.backend.pin.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.pin.dto.CreatePinRequest;
import com.ulog.backend.pin.dto.PinResponse;
import com.ulog.backend.pin.dto.PinSummaryResponse;
import com.ulog.backend.pin.dto.UpdatePinRequest;
import com.ulog.backend.pin.service.PinService;
import com.ulog.backend.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pins")
@Tag(name = "Pins", description = "Pin收藏管理接口")
@SecurityRequirement(name = "Bearer Authentication")
public class PinController {

    private final PinService pinService;

    public PinController(PinService pinService) {
        this.pinService = pinService;
    }

    @PostMapping
    @Operation(summary = "创建Pin", description = "从QA历史中Pin一个回答")
    public ApiResponse<PinResponse> createPin(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreatePinRequest request) {
        PinResponse response = pinService.createPin(principal.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "获取Pin列表", description = "获取用户所有的Pin，支持按联系人和来源类型筛选")
    public ApiResponse<List<PinSummaryResponse>> listPins(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long contactId,
            @RequestParam(required = false) String sourceType) {
        List<PinSummaryResponse> pins = pinService.listPins(principal.getUserId(), contactId, sourceType);
        return ApiResponse.success(pins);
    }

    @GetMapping("/{pinId}")
    @Operation(summary = "获取Pin详情", description = "获取指定Pin的完整内容")
    public ApiResponse<PinResponse> getPin(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long pinId) {
        PinResponse response = pinService.getPin(principal.getUserId(), pinId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{pinId}")
    @Operation(summary = "更新Pin", description = "更新Pin的备注和标签")
    public ApiResponse<PinResponse> updatePin(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long pinId,
            @Valid @RequestBody UpdatePinRequest request) {
        PinResponse response = pinService.updatePin(principal.getUserId(), pinId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{pinId}")
    @Operation(summary = "删除Pin", description = "删除指定的Pin")
    public ApiResponse<Void> deletePin(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long pinId) {
        pinService.deletePin(principal.getUserId(), pinId);
        return ApiResponse.success(null);
    }

    @GetMapping("/check")
    @Operation(summary = "检查是否已Pin", description = "检查指定的QA是否已被Pin")
    public ApiResponse<Boolean> checkPinned(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam String sessionId,
            @RequestParam Integer qaIndex) {
        boolean isPinned = pinService.isPinned(principal.getUserId(), sessionId, qaIndex);
        return ApiResponse.success(isPinned);
    }
}

