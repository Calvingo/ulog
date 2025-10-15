package com.ulog.backend.auth.service;

import com.ulog.backend.auth.dto.AuthResponse;
import com.ulog.backend.auth.dto.LoginRequest;
import com.ulog.backend.auth.dto.RegisterRequest;
import com.ulog.backend.auth.dto.TokenResponse;
import com.ulog.backend.common.api.ErrorCode;
import com.ulog.backend.common.exception.ApiException;
import com.ulog.backend.config.JwtProperties;
import com.ulog.backend.domain.token.RefreshToken;
import com.ulog.backend.domain.user.User;
import com.ulog.backend.repository.RefreshTokenRepository;
import com.ulog.backend.repository.UserRepository;
import com.ulog.backend.security.JwtTokenProvider;
import com.ulog.backend.user.dto.UserResponse;
import com.ulog.backend.util.RateLimiterService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final int LOGIN_FAIL_LIMIT = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;
    private final RateLimiterService rateLimiterService;
    private final SmsCodeService smsCodeService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider, JwtProperties jwtProperties, RateLimiterService rateLimiterService, SmsCodeService smsCodeService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.jwtProperties = jwtProperties;
        this.rateLimiterService = rateLimiterService;
        this.smsCodeService = smsCodeService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        rateLimiterService.checkRate("register:" + request.getPhone(), 3, Duration.ofMinutes(1));
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new ApiException(ErrorCode.USER_ALREADY_EXISTS, "phone already registered");
        }
        if (!smsCodeService.verify(request.getPhone(), request.getSmsCode())) {
            throw new ApiException(ErrorCode.SMS_CODE_INVALID, "invalid or expired sms code");
        }
        User user = new User(request.getPhone(), passwordEncoder.encode(request.getPassword()), request.getName());
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional(noRollbackFor = ApiException.class)
    public AuthResponse login(LoginRequest request) {
        rateLimiterService.checkRate("login:" + request.getPhone(), 10, Duration.ofMinutes(1));
        User user = userRepository.findByPhone(request.getPhone())
            .orElseThrow(() -> new ApiException(ErrorCode.LOGIN_FAILED, "invalid phone or password"));
        if (!user.isActive()) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "account is disabled");
        }
        if (user.isLocked()) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "account locked until " + user.getLockedUntil());
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new ApiException(ErrorCode.LOGIN_FAILED, "invalid phone or password");
        }
        resetLoginState(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public TokenResponse refresh(String refreshToken) {
        String type = tokenProvider.getTokenType(refreshToken);
        if (!"refresh".equals(type)) {
            throw new ApiException(ErrorCode.TOKEN_INVALID, "invalid token type");
        }
        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
            .orElseThrow(() -> new ApiException(ErrorCode.TOKEN_INVALID, "refresh token not found"));
        if (stored.isExpired()) {
            stored.setRevoked(true);
            throw new ApiException(ErrorCode.TOKEN_EXPIRED, "refresh token expired");
        }
        Long userId = tokenProvider.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(ErrorCode.AUTH_UNAUTHORIZED, "user not found"));
        if (!user.isActive()) {
            throw new ApiException(ErrorCode.ACCOUNT_LOCKED, "account disabled");
        }
        String accessToken = tokenProvider.generateAccessToken(user.getId());
        long accessExpiresIn = jwtProperties.getAccessTokenValidityMinutes() * 60;
        long refreshExpiresIn = Duration.between(LocalDateTime.now(), stored.getExpiresAt()).getSeconds();
        if (refreshExpiresIn < 0) {
            refreshExpiresIn = 0;
        }
        return new TokenResponse("Bearer", accessToken, accessExpiresIn, stored.getToken(), refreshExpiresIn);
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
            .ifPresent(token -> token.setRevoked(true));
    }

    private void handleFailedLogin(User user) {
        int attempts = Optional.ofNullable(user.getFailedAttempts()).orElse(0) + 1;
        user.setFailedAttempts(attempts);
        user.setLastFailedAt(LocalDateTime.now());
        if (attempts >= LOGIN_FAIL_LIMIT) {
            user.setFailedAttempts(0);
            user.setLockedUntil(LocalDateTime.now().plus(LOCK_DURATION));
        }
    }

    private void resetLoginState(User user) {
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        user.setLastFailedAt(null);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = tokenProvider.generateAccessToken(user.getId());
        String refreshToken = tokenProvider.generateRefreshToken(user.getId());
        long accessExpiresIn = jwtProperties.getAccessTokenValidityMinutes() * 60;
        long refreshExpiresIn = jwtProperties.getRefreshTokenValidityDays() * 24 * 3600;
        RefreshToken store = new RefreshToken(user, refreshToken, tokenProvider.getExpiry(refreshToken), false);
        refreshTokenRepository.save(store);
        UserResponse userResponse = new UserResponse(user.getId(), maskPhone(user.getPhone()), user.getName(), user.getDescription(), user.getAiSummary(), user.getStatus(), user.getCreatedAt(), user.getUpdatedAt());
        return new AuthResponse(userResponse, new TokenResponse("Bearer", accessToken, accessExpiresIn, refreshToken, refreshExpiresIn));
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
