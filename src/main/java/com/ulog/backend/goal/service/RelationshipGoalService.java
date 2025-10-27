package com.ulog.backend.goal.service;

import com.ulog.backend.ai.GoalAiService;
import com.ulog.backend.ai.dto.AiActionPlanItem;
import com.ulog.backend.ai.dto.AiGoalStrategyResponse;
import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.goal.ActionPlan;
import com.ulog.backend.domain.goal.RelationshipGoal;
import com.ulog.backend.domain.goal.enums.ActionPlanStatus;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.goal.dto.ActionPlanResponse;
import com.ulog.backend.goal.dto.CreateActionPlanRequest;
import com.ulog.backend.goal.dto.CreateGoalRequest;
import com.ulog.backend.goal.dto.GoalDetailResponse;
import com.ulog.backend.goal.dto.GoalResponse;
import com.ulog.backend.goal.dto.UpdateActionPlanAdoptionRequest;
import com.ulog.backend.goal.dto.UpdateActionPlanRequest;
import com.ulog.backend.goal.dto.UpdateActionPlanStatusRequest;
import com.ulog.backend.goal.dto.UpdateGoalRequest;
import com.ulog.backend.repository.ActionPlanRepository;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.RelationshipGoalRepository;
import com.ulog.backend.repository.UserRepository;
import com.ulog.backend.compliance.service.OperationLogService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelationshipGoalService {

    private static final Logger log = LoggerFactory.getLogger(RelationshipGoalService.class);

    private final RelationshipGoalRepository goalRepository;
    private final ActionPlanRepository actionPlanRepository;
    private final ContactRepository contactRepository;
    private final UserRepository userRepository;
    private final GoalAiService goalAiService;
    private final ReminderService reminderService;
    private final OperationLogService operationLogService;

    public RelationshipGoalService(RelationshipGoalRepository goalRepository,
                                   ActionPlanRepository actionPlanRepository,
                                   ContactRepository contactRepository,
                                   UserRepository userRepository,
                                   GoalAiService goalAiService,
                                   ReminderService reminderService,
                                   OperationLogService operationLogService) {
        this.goalRepository = goalRepository;
        this.actionPlanRepository = actionPlanRepository;
        this.contactRepository = contactRepository;
        this.userRepository = userRepository;
        this.goalAiService = goalAiService;
        this.reminderService = reminderService;
        this.operationLogService = operationLogService;
    }

    @Transactional
    public GoalDetailResponse createGoal(Long userId, CreateGoalRequest request) {
        User user = loadUser(userId);
        Contact contact = loadContact(userId, request.getContactId());

        // 创建目标
        RelationshipGoal goal = new RelationshipGoal(contact, user, request.getGoalDescription());
        goalRepository.save(goal);
        
        // 记录关系目标创建日志
        operationLogService.logOperation(userId, "goal_create", 
            String.format("Created relationship goal: %s for contact: %s", 
                goal.getId(), contact.getName()));

        // 调用AI生成策略和行动计划
        try {
            String contactInfo = buildContactInfo(contact);
            String userInfo = buildUserInfo(user);
            AiGoalStrategyResponse aiResponse = goalAiService
                .generateGoalStrategy(contactInfo, userInfo, request.getGoalDescription())
                .block();

            if (aiResponse != null) {
                // 保存AI策略
                goal.setAiStrategy(aiResponse.getStrategy());

                // 创建行动计划
                List<ActionPlan> actionPlans = createActionPlans(goal, aiResponse.getActionPlans());
                
                // 为已采纳的行动计划创建提醒
                for (ActionPlan plan : actionPlans) {
                    if (Boolean.TRUE.equals(plan.getIsAdopted())) {
                        reminderService.createRemindersForActionPlan(plan);
                    }
                }

                log.info("Created goal {} with {} action plans for user {}", 
                         goal.getId(), actionPlans.size(), userId);
            }
        } catch (Exception e) {
            log.error("Failed to generate AI strategy for goal {}: {}", goal.getId(), e.getMessage());
            // 继续保存目标，即使AI生成失败
        }

        return getGoal(userId, goal.getId());
    }

    @Transactional(readOnly = true)
    public GoalDetailResponse getGoal(Long userId, Long goalId) {
        RelationshipGoal goal = findOwnedGoal(userId, goalId);
        List<ActionPlan> actionPlans = actionPlanRepository.findAllByGoalAndDeletedFalseOrderByOrderIndexAsc(goal);

        List<ActionPlanResponse> planResponses = actionPlans.stream()
            .map(this::mapActionPlanToResponse)
            .collect(Collectors.toList());

        return new GoalDetailResponse(
            goal.getId(),
            goal.getContact().getId(),
            goal.getContact().getName(),
            goal.getGoalDescription(),
            goal.getAiStrategy(),
            goal.getStatus(),
            planResponses,
            goal.getCreatedAt(),
            goal.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> listGoals(Long userId, Long contactId) {
        User user = loadUser(userId);
        List<RelationshipGoal> goals;

        if (contactId != null) {
            Contact contact = loadContact(userId, contactId);
            goals = goalRepository.findAllByUserAndContactAndDeletedFalseOrderByCreatedAtDesc(user, contact);
        } else {
            goals = goalRepository.findAllByUserAndDeletedFalseOrderByCreatedAtDesc(user);
        }

        return goals.stream()
            .map(this::mapGoalToResponse)
            .collect(Collectors.toList());
    }

    @Transactional
    public GoalDetailResponse updateGoal(Long userId, Long goalId, UpdateGoalRequest request) {
        RelationshipGoal goal = findOwnedGoal(userId, goalId);

        if (request.getGoalDescription() == null && request.getStatus() == null) {
            throw new BadRequestException("No fields to update");
        }

        if (request.getGoalDescription() != null) {
            goal.setGoalDescription(request.getGoalDescription());
        }

        if (request.getStatus() != null) {
            goal.setStatus(request.getStatus());
        }

        goalRepository.save(goal);
        return getGoal(userId, goalId);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        RelationshipGoal goal = findOwnedGoal(userId, goalId);
        goal.setDeleted(true);
        goalRepository.save(goal);
        log.info("Deleted goal {} for user {}", goalId, userId);
    }

    @Transactional
    public ActionPlanResponse updateActionPlanStatus(Long userId, Long planId, 
                                                      UpdateActionPlanStatusRequest request) {
        ActionPlan plan = actionPlanRepository.findByIdAndDeletedFalse(planId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Action plan not found"));

        // 验证所有权
        if (!plan.getGoal().getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Cannot update another user's action plan");
        }

        plan.setStatus(request.getStatus());
        
        if (request.getCompletedAt() != null) {
            plan.setCompletedAt(request.getCompletedAt());
        } else if (request.getStatus() == ActionPlanStatus.COMPLETED) {
            plan.setCompletedAt(LocalDateTime.now());
        }

        actionPlanRepository.save(plan);
        log.info("Updated action plan {} status to {} for user {}", planId, request.getStatus(), userId);

        return mapActionPlanToResponse(plan);
    }

    @Transactional
    public ActionPlanResponse updateActionPlanAdoption(Long userId, Long planId, 
                                                       UpdateActionPlanAdoptionRequest request) {
        ActionPlan plan = actionPlanRepository.findByIdAndDeletedFalse(planId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Action plan not found"));

        // 验证所有权
        if (!plan.getGoal().getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Cannot update another user's action plan");
        }

        boolean wasAdopted = Boolean.TRUE.equals(plan.getIsAdopted());
        boolean willBeAdopted = Boolean.TRUE.equals(request.getIsAdopted());

        plan.setIsAdopted(request.getIsAdopted());
        actionPlanRepository.save(plan);

        // 如果从未采纳变为采纳，创建提醒
        if (!wasAdopted && willBeAdopted) {
            reminderService.createRemindersForActionPlan(plan);
            log.info("Created reminders for newly adopted action plan {}", planId);
        }
        // 如果从采纳变为未采纳，取消提醒
        else if (wasAdopted && !willBeAdopted) {
            reminderService.cancelRemindersForActionPlan(plan);
            log.info("Cancelled reminders for un-adopted action plan {}", planId);
        }

        log.info("Updated action plan {} adoption to {} for user {}", planId, request.getIsAdopted(), userId);

        return mapActionPlanToResponse(plan);
    }

    @Transactional
    public GoalDetailResponse regenerateStrategy(Long userId, Long goalId) {
        RelationshipGoal goal = findOwnedGoal(userId, goalId);
        Contact contact = goal.getContact();

        try {
            String contactInfo = buildContactInfo(contact);
            String userInfo = buildUserInfo(goal.getUser());
            AiGoalStrategyResponse aiResponse = goalAiService
                .generateGoalStrategy(contactInfo, userInfo, goal.getGoalDescription())
                .block();

            if (aiResponse != null) {
                goal.setAiStrategy(aiResponse.getStrategy());

                // 删除旧的行动计划（软删除）
                List<ActionPlan> oldPlans = actionPlanRepository
                    .findAllByGoalAndDeletedFalseOrderByOrderIndexAsc(goal);
                for (ActionPlan oldPlan : oldPlans) {
                    oldPlan.setDeleted(true);
                    // 取消旧计划的提醒
                    reminderService.cancelRemindersForActionPlan(oldPlan);
                }

                // 创建新的行动计划
                List<ActionPlan> newPlans = createActionPlans(goal, aiResponse.getActionPlans());
                
                // 为已采纳的行动计划创建提醒
                for (ActionPlan plan : newPlans) {
                    if (Boolean.TRUE.equals(plan.getIsAdopted())) {
                        reminderService.createRemindersForActionPlan(plan);
                    }
                }

                log.info("Regenerated strategy for goal {} with {} new action plans", 
                         goalId, newPlans.size());
            }
        } catch (Exception e) {
            log.error("Failed to regenerate strategy for goal {}: {}", goalId, e.getMessage());
            throw new RuntimeException("Failed to regenerate strategy: " + e.getMessage(), e);
        }

        return getGoal(userId, goalId);
    }

    @Transactional
    public ActionPlanResponse createActionPlan(Long userId, Long goalId, CreateActionPlanRequest request) {
        RelationshipGoal goal = findOwnedGoal(userId, goalId);
        
        // 获取当前最大的orderIndex
        List<ActionPlan> existingPlans = actionPlanRepository.findAllByGoalAndDeletedFalseOrderByOrderIndexAsc(goal);
        int maxOrderIndex = existingPlans.stream()
            .mapToInt(ActionPlan::getOrderIndex)
            .max()
            .orElse(-1);
        
        // 创建新的action plan
        ActionPlan plan = new ActionPlan();
        plan.setGoal(goal);
        plan.setTitle(request.getTitle());
        plan.setDescription(request.getDescription());
        plan.setScheduledTime(request.getScheduledTime());
        plan.setIsAdopted(request.getIsAdopted());
        plan.setOrderIndex(maxOrderIndex + 1);
        plan.setStatus(ActionPlanStatus.PENDING);
        plan.setDeleted(Boolean.FALSE);
        
        actionPlanRepository.save(plan);
        
        // 如果采纳了，创建提醒
        if (Boolean.TRUE.equals(plan.getIsAdopted())) {
            reminderService.createRemindersForActionPlan(plan);
        }
        
        log.info("Created manual action plan {} for goal {} by user {}", plan.getId(), goalId, userId);
        
        return mapActionPlanToResponse(plan);
    }

    @Transactional
    public ActionPlanResponse updateActionPlan(Long userId, Long planId, UpdateActionPlanRequest request) {
        ActionPlan plan = actionPlanRepository.findByIdAndDeletedFalse(planId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Action plan not found"));

        // 验证所有权
        if (!plan.getGoal().getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Cannot update another user's action plan");
        }

        boolean scheduledTimeChanged = false;
        
        if (request.getTitle() != null) {
            plan.setTitle(request.getTitle());
        }
        
        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }
        
        if (request.getScheduledTime() != null) {
            if (!request.getScheduledTime().equals(plan.getScheduledTime())) {
                scheduledTimeChanged = true;
            }
            plan.setScheduledTime(request.getScheduledTime());
        }

        actionPlanRepository.save(plan);
        
        // 如果时间改变了且计划被采纳，需要更新提醒
        if (scheduledTimeChanged && Boolean.TRUE.equals(plan.getIsAdopted())) {
            reminderService.cancelRemindersForActionPlan(plan);
            reminderService.createRemindersForActionPlan(plan);
        }
        
        log.info("Updated action plan {} for user {}", planId, userId);

        return mapActionPlanToResponse(plan);
    }

    @Transactional
    public void deleteActionPlan(Long userId, Long planId) {
        ActionPlan plan = actionPlanRepository.findByIdAndDeletedFalse(planId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Action plan not found"));

        // 验证所有权
        if (!plan.getGoal().getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Cannot delete another user's action plan");
        }

        // 取消相关提醒
        reminderService.cancelRemindersForActionPlan(plan);
        
        // 软删除
        plan.setDeleted(Boolean.TRUE);
        actionPlanRepository.save(plan);
        
        log.info("Deleted action plan {} for user {}", planId, userId);
    }

    private List<ActionPlan> createActionPlans(RelationshipGoal goal, 
                                               List<AiActionPlanItem> aiItems) {
        List<ActionPlan> actionPlans = new ArrayList<>();
        int orderIndex = 0;

        for (AiActionPlanItem item : aiItems) {
            LocalDateTime scheduledTime = LocalDateTime.now().plusDays(item.getScheduledDays());
            ActionPlan plan = new ActionPlan(goal, item.getTitle(), item.getDescription(), 
                                            scheduledTime, orderIndex++);
            actionPlanRepository.save(plan);
            actionPlans.add(plan);
        }

        return actionPlans;
    }

    private String buildContactInfo(Contact contact) {
        StringBuilder info = new StringBuilder();
        info.append("姓名：").append(contact.getName()).append("\n");
        
        if (contact.getDescription() != null && !contact.getDescription().isBlank()) {
            info.append("描述：").append(contact.getDescription()).append("\n");
        }
        
        if (contact.getSelfValue() != null && !contact.getSelfValue().isBlank()) {
            info.append("对我的价值/定位：").append(contact.getSelfValue()).append("\n");
        }
        
        return info.toString();
    }

    private String buildUserInfo(User user) {
        StringBuilder info = new StringBuilder();
        info.append("姓名：").append(user.getName()).append("\n");
        
        if (user.getDescription() != null && !user.getDescription().isBlank()) {
            info.append("描述：").append(user.getDescription()).append("\n");
        }
        
        if (user.getSelfValue() != null && !user.getSelfValue().isBlank()) {
            info.append("我的价值观/定位：").append(user.getSelfValue()).append("\n");
        }
        
        return info.toString();
    }

    private RelationshipGoal findOwnedGoal(Long userId, Long goalId) {
        User user = loadUser(userId);
        return goalRepository.findByIdAndUserAndDeletedFalse(goalId, user)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Goal not found"));
    }

    private Contact loadContact(Long userId, Long contactId) {
        User user = loadUser(userId);
        return contactRepository.findByIdAndOwnerAndDeletedFalse(contactId, user)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Contact not found"));
    }

    private User loadUser(Long userId) {
        return userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "User not found"));
    }

    private GoalResponse mapGoalToResponse(RelationshipGoal goal) {
        return new GoalResponse(
            goal.getId(),
            goal.getContact().getId(),
            goal.getContact().getName(),
            goal.getGoalDescription(),
            goal.getAiStrategy(),
            goal.getStatus(),
            goal.getCreatedAt(),
            goal.getUpdatedAt()
        );
    }

    private ActionPlanResponse mapActionPlanToResponse(ActionPlan plan) {
        return new ActionPlanResponse(
            plan.getId(),
            plan.getTitle(),
            plan.getDescription(),
            plan.getScheduledTime(),
            plan.getIsAdopted(),
            plan.getStatus(),
            plan.getCompletedAt(),
            plan.getOrderIndex(),
            plan.getCreatedAt(),
            plan.getUpdatedAt()
        );
    }
}

