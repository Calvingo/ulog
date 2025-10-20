package com.ulog.backend.compliance.dto;

import java.time.LocalDateTime;

public class PrivacyConsentResponse {

    private Long id;
    private Long userId;
    private String policyVersion;
    private LocalDateTime consentTime;
    private boolean hasConsented;

    public PrivacyConsentResponse() {
    }

    public PrivacyConsentResponse(Long id, Long userId, String policyVersion, 
                                 LocalDateTime consentTime, boolean hasConsented) {
        this.id = id;
        this.userId = userId;
        this.policyVersion = policyVersion;
        this.consentTime = consentTime;
        this.hasConsented = hasConsented;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public LocalDateTime getConsentTime() {
        return consentTime;
    }

    public void setConsentTime(LocalDateTime consentTime) {
        this.consentTime = consentTime;
    }

    public boolean isHasConsented() {
        return hasConsented;
    }

    public void setHasConsented(boolean hasConsented) {
        this.hasConsented = hasConsented;
    }
}

