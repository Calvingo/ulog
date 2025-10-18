package com.ulog.backend.conversation.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.conversation.dto.*;
import com.ulog.backend.conversation.service.InfoCollectionService;
import com.ulog.backend.conversation.service.QaService;
import com.ulog.backend.security.UserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/conversation")
public class ConversationController {
    
    private final InfoCollectionService infoCollectionService;
    private final QaService qaService;
    
    public ConversationController(
        InfoCollectionService infoCollectionService,
        QaService qaService
    ) {
        this.infoCollectionService = infoCollectionService;
        this.qaService = qaService;
    }
    
    // ========== 信息收集模式 ==========
    
    /**
     * 开始信息收集
     */
    @PostMapping("/collect/start")
    public ResponseEntity<ApiResponse<StartCollectionResponse>> startCollection(
        @AuthenticationPrincipal UserPrincipal principal,
        @Valid @RequestBody StartCollectionRequest request
    ) {
        StartCollectionResponse response = infoCollectionService.startCollection(
            principal.getUserId(),
            request.getContactName()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response));
    }
    
    /**
     * 发送消息（多轮对话）
     */
    @PostMapping("/collect/{sessionId}/message")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId,
        @Valid @RequestBody MessageRequest request
    ) {
        MessageResponse response = infoCollectionService.processMessage(
            sessionId,
            principal.getUserId(),
            request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 获取收集进度
     */
    @GetMapping("/collect/{sessionId}/progress")
    public ResponseEntity<ApiResponse<Integer>> getProgress(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId
    ) {
        Integer progress = infoCollectionService.getProgress(
            sessionId,
            principal.getUserId()
        );
        return ResponseEntity.ok(ApiResponse.success(progress));
    }
    
    /**
     * 放弃会话
     */
    @PostMapping("/collect/{sessionId}/abandon")
    public ResponseEntity<ApiResponse<Void>> abandonSession(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId
    ) {
        infoCollectionService.abandonSession(sessionId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
    
    // ========== 问答模式 ==========
    
    /**
     * 问答模式：提问
     */
    @PostMapping("/qa/{sessionId}/message")
    public ResponseEntity<ApiResponse<QaResponse>> askQuestion(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId,
        @Valid @RequestBody QaRequest request
    ) {
        QaResponse response = qaService.processQuestion(
            sessionId,
            principal.getUserId(),
            request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 生成AI总结
     */
    @PostMapping("/qa/{sessionId}/generate-summary")
    public ResponseEntity<ApiResponse<String>> generateSummary(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId
    ) {
        String summary = qaService.generateSummary(sessionId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(summary));
    }
    
    /**
     * 补充信息
     */
    @PostMapping("/qa/{sessionId}/supplement")
    public ResponseEntity<ApiResponse<QaResponse>> supplementInfo(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId,
        @Valid @RequestBody SupplementRequest request
    ) {
        QaResponse response = qaService.processSupplementInfo(
            sessionId,
            principal.getUserId(),
            request.getSupplementInfo()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * 结束会话
     */
    @PostMapping("/qa/{sessionId}/end")
    public ResponseEntity<ApiResponse<Void>> endSession(
        @AuthenticationPrincipal UserPrincipal principal,
        @PathVariable String sessionId
    ) {
        qaService.endSession(sessionId, principal.getUserId());
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}

