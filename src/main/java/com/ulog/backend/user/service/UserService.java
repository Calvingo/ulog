package com.ulog.backend.user.service;

import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.common.exception.BadRequestException;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.UserRepository;
import com.ulog.backend.user.dto.ChangePasswordRequest;
import com.ulog.backend.user.dto.UserResponse;
import com.ulog.backend.user.dto.UserUpdateRequest;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
