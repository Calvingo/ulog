package com.ulog.backend.goal.controller;

import com.ulog.backend.common.api.ApiResponse;
import com.ulog.backend.goal.dto.ActionPlanResponse;
import com.ulog.backend.goal.dto.CreateActionPlanRequest;
import com.ulog.backend.goal.dto.CreateGoalRequest;
import com.ulog.backend.goal.dto.GoalDetailResponse;
import com.ulog.backend.goal.dto.GoalResponse;
import com.ulog.backend.goal.dto.ReminderResponse;
import com.ulog.backend.goal.dto.UpdateActionPlanAdoptionRequest;
import com.ulog.backend.goal.dto.UpdateActionPlanRequest;
import com.ulog.backend.goal.dto.UpdateActionPlanStatusRequest;
import com.ulog.backend.goal.dto.UpdateGoalRequest;
import com.ulog.backend.goal.service.RelationshipGoalService;
import com.ulog.backend.goal.service.ReminderService;
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
@RequestMapping("/api/goals")
@Tag(name = "Relationship Goals", description = "关系目标管理接口")
@SecurityRequirement(name = "Bearer Authentication")
public class RelationshipGoalController {

    private final RelationshipGoalService goalService;
    private final ReminderService reminderService;

    public RelationshipGoalController(RelationshipGoalService goalService,
                                     ReminderService reminderService) {
        this.goalService = goalService;
        this.reminderService = reminderService;
    }

    @PostMapping
    @Operation(summary = "创建关系目标", description = "为指定联系人创建关系目标，系统会自动生成策略和行动计划")
    public ApiResponse<GoalDetailResponse> createGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateGoalRequest request) {
        GoalDetailResponse response = goalService.createGoal(principal.getUserId(), request);
        return ApiResponse.success(response);
    }

    @GetMapping
    @Operation(summary = "列出关系目标", description = "获取用户的所有关系目标，可选择性地按联系人筛选")
    public ApiResponse<List<GoalResponse>> listGoals(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) Long contactId) {
        List<GoalResponse> goals = goalService.listGoals(principal.getUserId(), contactId);
        return ApiResponse.success(goals);
    }

    @GetMapping("/{goalId}")
    @Operation(summary = "获取目标详情", description = "获取指定关系目标的详细信息，包括所有行动计划")
    public ApiResponse<GoalDetailResponse> getGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId) {
        GoalDetailResponse response = goalService.getGoal(principal.getUserId(), goalId);
        return ApiResponse.success(response);
    }

    @PutMapping("/{goalId}")
    @Operation(summary = "更新关系目标", description = "更新关系目标的描述或状态")
    public ApiResponse<GoalDetailResponse> updateGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId,
            @Valid @RequestBody UpdateGoalRequest request) {
        GoalDetailResponse response = goalService.updateGoal(principal.getUserId(), goalId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{goalId}")
    @Operation(summary = "删除关系目标", description = "软删除指定的关系目标")
    public ApiResponse<Void> deleteGoal(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId) {
        goalService.deleteGoal(principal.getUserId(), goalId);
        return ApiResponse.success(null);
    }

    @PutMapping("/{goalId}/action-plans/{planId}/status")
    @Operation(summary = "更新行动计划状态", description = "更新指定行动计划的执行状态")
    public ApiResponse<ActionPlanResponse> updateActionPlanStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId,
            @PathVariable Long planId,
            @Valid @RequestBody UpdateActionPlanStatusRequest request) {
        ActionPlanResponse response = goalService.updateActionPlanStatus(
            principal.getUserId(), planId, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{goalId}/action-plans/{planId}/adoption")
    @Operation(summary = "更新行动计划采纳状态", 
               description = "标记是否采纳该行动计划。采纳后会创建提醒，取消采纳会取消相关提醒")
    public ApiResponse<ActionPlanResponse> updateActionPlanAdoption(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId,
            @PathVariable Long planId,
            @Valid @RequestBody UpdateActionPlanAdoptionRequest request) {
        ActionPlanResponse response = goalService.updateActionPlanAdoption(
            principal.getUserId(), planId, request);
        return ApiResponse.success(response);
    }

    @PostMapping("/{goalId}/regenerate")
    @Operation(summary = "重新生成策略", description = "重新生成AI策略和行动计划")
    public ApiResponse<GoalDetailResponse> regenerateStrategy(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId) {
        GoalDetailResponse response = goalService.regenerateStrategy(principal.getUserId(), goalId);
        return ApiResponse.success(response);
    }

    @PostMapping("/{goalId}/action-plans")
    @Operation(summary = "创建行动计划", description = "为指定目标手动创建行动计划")
    public ApiResponse<ActionPlanResponse> createActionPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId,
            @Valid @RequestBody CreateActionPlanRequest request) {
        ActionPlanResponse response = goalService.createActionPlan(principal.getUserId(), goalId, request);
        return ApiResponse.success(response);
    }

    @PutMapping("/{goalId}/action-plans/{planId}")
    @Operation(summary = "更新行动计划", description = "更新指定行动计划的基本信息")
    public ApiResponse<ActionPlanResponse> updateActionPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId,
            @PathVariable Long planId,
            @Valid @RequestBody UpdateActionPlanRequest request) {
        ActionPlanResponse response = goalService.updateActionPlan(principal.getUserId(), planId, request);
        return ApiResponse.success(response);
    }

    @DeleteMapping("/{goalId}/action-plans/{planId}")
    @Operation(summary = "删除行动计划", description = "软删除指定的行动计划")
    public ApiResponse<Void> deleteActionPlan(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long goalId,
            @PathVariable Long planId) {
        goalService.deleteActionPlan(principal.getUserId(), planId);
        return ApiResponse.success(null);
    }

    @GetMapping("/reminders/upcoming")
    @Operation(summary = "获取即将到来的提醒", description = "获取用户所有即将到来的行动计划提醒")
    public ApiResponse<List<ReminderResponse>> getUpcomingReminders(
            @AuthenticationPrincipal UserPrincipal principal) {
        List<ReminderResponse> reminders = reminderService.getUpcomingReminders(principal.getUserId());
        return ApiResponse.success(reminders);
    }
}

