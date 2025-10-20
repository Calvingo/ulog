package com.ulog.backend.compliance.service;

import com.ulog.backend.config.PrivacyPolicyProperties;
import com.ulog.backend.domain.compliance.UserPrivacyConsent;
import com.ulog.backend.repository.UserPrivacyConsentRepository;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PrivacyConsentService {

    private static final Logger log = LoggerFactory.getLogger(PrivacyConsentService.class);

    private final UserPrivacyConsentRepository consentRepository;
    private final PrivacyPolicyProperties policyProperties;

    public PrivacyConsentService(UserPrivacyConsentRepository consentRepository,
                                PrivacyPolicyProperties policyProperties) {
        this.consentRepository = consentRepository;
        this.policyProperties = policyProperties;
    }

    /**
     * 记录用户隐私协议同意
     */
    @Transactional
    public UserPrivacyConsent recordConsent(Long userId, String policyVersion, 
                                           String ipAddress, String userAgent) {
        log.info("Recording privacy consent for user {} with version {}", userId, policyVersion);

        UserPrivacyConsent consent = new UserPrivacyConsent(userId, policyVersion, ipAddress, userAgent);
        return consentRepository.save(consent);
    }

    /**
     * 检查用户是否已同意当前版本的隐私政策
     */
    @Transactional(readOnly = true)
    public boolean hasConsentedToCurrentPolicy(Long userId) {
        String currentVersion = policyProperties.getVersion();
        Optional<UserPrivacyConsent> consent = consentRepository
            .findLatestByUserIdAndVersion(userId, currentVersion);
        
        boolean hasConsented = consent.isPresent();
        log.debug("User {} consent status for version {}: {}", userId, currentVersion, hasConsented);
        
        return hasConsented;
    }

    /**
     * 获取用户最新的隐私协议同意记录
     */
    @Transactional(readOnly = true)
    public Optional<UserPrivacyConsent> getLatestConsent(Long userId) {
        return consentRepository.findLatestByUserId(userId);
    }

    /**
     * 获取用户所有隐私协议同意记录
     */
    @Transactional(readOnly = true)
    public List<UserPrivacyConsent> getUserConsents(Long userId) {
        return consentRepository.findByUserIdOrderByConsentTimeDesc(userId);
    }

    /**
     * 获取当前隐私政策版本
     */
    public String getCurrentPolicyVersion() {
        return policyProperties.getVersion();
    }

    /**
     * 获取隐私政策URL
     */
    public String getPolicyUrl() {
        return policyProperties.getUrl();
    }

    /**
     * 检查是否需要用户同意隐私政策
     */
    public boolean isPolicyConsentRequired() {
        return policyProperties.isRequired();
    }
}

