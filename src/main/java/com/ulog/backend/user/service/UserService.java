package com.ulog.backend.user.service;

import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.compliance.service.OperationLogService;
import com.ulog.backend.conversation.service.SelfValueCalculationService;
import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.goal.RelationshipGoal;
import com.ulog.backend.domain.goal.UserPushToken;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.ContactRepository;
import com.ulog.backend.repository.RefreshTokenRepository;
import com.ulog.backend.repository.RelationshipGoalRepository;
import com.ulog.backend.repository.UserPushTokenRepository;
import com.ulog.backend.repository.UserRepository;
import com.ulog.backend.user.dto.ChangePasswordRequest;
import com.ulog.backend.user.dto.DeleteAccountRequest;
import com.ulog.backend.user.dto.UserResponse;
import com.ulog.backend.user.dto.UserUpdateRequest;
import java.util.List;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ContactRepository contactRepository;
    private final RelationshipGoalRepository relationshipGoalRepository;
    private final UserPushTokenRepository userPushTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OperationLogService operationLogService;
    private final SelfValueCalculationService selfValueCalculationService;

    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      ContactRepository contactRepository,
                      RelationshipGoalRepository relationshipGoalRepository,
                      UserPushTokenRepository userPushTokenRepository,
                      RefreshTokenRepository refreshTokenRepository,
                      OperationLogService operationLogService,
                      SelfValueCalculationService selfValueCalculationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.contactRepository = contactRepository;
        this.relationshipGoalRepository = relationshipGoalRepository;
        this.userPushTokenRepository = userPushTokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.operationLogService = operationLogService;
        this.selfValueCalculationService = selfValueCalculationService;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        return mapToResponse(user, false);
    }

    @Transactional
    public UserResponse updateCurrentUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        if (request.getName() == null && request.getDescription() == null && request.getAiSummary() == null) {
            throw new BadRequestException("no fields to update");
        }
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getDescription() != null) {
            user.setDescription(request.getDescription());
        }
        if (request.getAiSummary() != null) {
            user.setAiSummary(request.getAiSummary());
        }
        return mapToResponse(user, false);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "current password incorrect");
        }
        if (Objects.equals(request.getCurrentPassword(), request.getNewPassword())) {
            throw new BadRequestException("new password must differ from current password");
        }
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        
        // 记录密码修改操作
        operationLogService.logOperation(userId, "password_change", "User changed password");
    }

    @Transactional
    public void deleteAccount(Long userId, DeleteAccountRequest request) {
        User user = userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(ErrorCode.BAD_REQUEST, "current password incorrect");
        }

        // 记录账号删除操作日志（在实际删除前记录）
        String operationDetail = String.format("User %s (phone: %s) requested account deletion", 
            userId, user.getPhone());
        operationLogService.logOperation(userId, "account_delete", operationDetail);

        // 清理关联数据
        cleanupUserRelatedData(user);

        // 撤销所有refresh tokens
        refreshTokenRepository.revokeAllForUser(user);

        // 标记用户为已删除（软删除，保留审计数据）
        user.setDeleted(Boolean.TRUE);
        user.setStatus(0); // 设置为非活跃状态
        userRepository.save(user);
        
        log.info("User account deleted: userId={}, phone={}", userId, user.getPhone());
    }

    /**
     * 清理用户关联数据
     */
    private void cleanupUserRelatedData(User user) {
        // 1. 删除用户的所有联系人（软删除）
        List<Contact> contacts = contactRepository.findAllByOwnerAndDeletedFalseOrderByCreatedAtDesc(user);
        for (Contact contact : contacts) {
            contact.setDeleted(Boolean.TRUE);
            contactRepository.save(contact);
        }

        // 2. 删除用户的所有关系目标（软删除）
        List<RelationshipGoal> goals = relationshipGoalRepository.findAllByUserAndDeletedFalseOrderByCreatedAtDesc(user);
        for (RelationshipGoal goal : goals) {
            goal.setDeleted(Boolean.TRUE);
            relationshipGoalRepository.save(goal);
        }

        // 3. 删除用户的所有推送令牌
        List<UserPushToken> pushTokens = userPushTokenRepository.findAllByUserAndIsActiveTrue(user);
        userPushTokenRepository.deleteAll(pushTokens);

        // 注意：conversation_sessions, user_conversation_sessions, pins 等表
        // 已配置 ON DELETE CASCADE，删除用户时会自动级联删除
    }
    
    /**
     * 更新用户描述（用于自我信息收集）
     */
    @Transactional
    public void updateUserDescription(Long userId, String description) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        user.setDescription(description);
        userRepository.save(user);
        
        // 🔥 异步重新计算 selfValue（基于新的description）
        if (description != null && !description.trim().isEmpty()) {
            selfValueCalculationService.calculateAndUpdateUserAsync(userId, description);
        }
    }
    
    /**
     * 获取用户描述
     */
    @Transactional(readOnly = true)
    public String getUserDescription(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        return user.getDescription();
    }

    /**
     * 更新用户自我价值评分
     */
    @Transactional
    public void updateUserSelfValue(Long userId, String selfValue) {
        log.info("Updating self value for user {}: {}", userId, selfValue);
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        
        user.setSelfValue(selfValue);
        userRepository.save(user);
        
        log.info("Successfully updated self value for user {}", userId);
    }

    private UserResponse mapToResponse(User user, boolean maskPhone) {
        String phoneValue = maskPhone ? maskPhone(user.getPhone()) : user.getPhone();
        return new UserResponse(user.getId(), phoneValue, user.getName(), user.getDescription(), user.getAiSummary(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) {
            return phone;
        }
        int unmasked = 4;
        int maskedLength = phone.length() - unmasked;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maskedLength; i++) {
            sb.append('*');
        }
        sb.append(phone.substring(maskedLength));
        return sb.toString();
    }
}
