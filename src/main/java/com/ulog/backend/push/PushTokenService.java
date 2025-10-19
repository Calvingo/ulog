package com.ulog.backend.push;

import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.domain.goal.UserPushToken;
import com.ulog.backend.domain.goal.enums.DeviceType;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.goal.dto.RegisterPushTokenRequest;
import com.ulog.backend.repository.UserPushTokenRepository;
import com.ulog.backend.repository.UserRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushTokenService {

    private static final Logger log = LoggerFactory.getLogger(PushTokenService.class);

    private final UserPushTokenRepository userPushTokenRepository;
    private final UserRepository userRepository;

    public PushTokenService(UserPushTokenRepository userPushTokenRepository, 
                           UserRepository userRepository) {
        this.userPushTokenRepository = userPushTokenRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void registerToken(Long userId, RegisterPushTokenRequest request) {
        User user = loadUser(userId);

        // 检查该设备令牌是否已存在
        userPushTokenRepository.findByDeviceToken(request.getDeviceToken())
            .ifPresentOrElse(
                existingToken -> {
                    // 如果存在，更新为当前用户并激活
                    if (!existingToken.getUser().getId().equals(userId)) {
                        existingToken.setUser(user);
                    }
                    existingToken.setDeviceType(request.getDeviceType());
                    existingToken.setIsActive(true);
                    userPushTokenRepository.save(existingToken);
                    log.info("Updated existing push token {} for user {}", existingToken.getId(), userId);
                },
                () -> {
                    // 不存在，创建新的
                    UserPushToken newToken = new UserPushToken(user, request.getDeviceToken(), 
                                                               request.getDeviceType());
                    userPushTokenRepository.save(newToken);
                    log.info("Registered new push token for user {}", userId);
                }
            );
    }

    @Transactional
    public void deactivateToken(Long userId, Long tokenId) {
        UserPushToken token = userPushTokenRepository.findById(tokenId)
            .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Push token not found"));

        if (!token.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCode.FORBIDDEN, "Cannot deactivate another user's token");
        }

        token.setIsActive(false);
        userPushTokenRepository.save(token);
        log.info("Deactivated push token {} for user {}", tokenId, userId);
    }

    @Transactional(readOnly = true)
    public List<UserPushToken> getUserActiveTokens(Long userId) {
        User user = loadUser(userId);
        return userPushTokenRepository.findAllByUserAndIsActiveTrue(user);
    }

    private User loadUser(Long userId) {
        return userRepository.findActiveById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
    }
}

