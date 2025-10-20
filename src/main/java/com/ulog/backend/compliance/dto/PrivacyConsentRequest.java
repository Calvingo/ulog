package com.ulog.backend.compliance.dto;

import jakarta.validation.constraints.NotBlank;

public class PrivacyConsentRequest {

    @NotBlank(message = "Policy version is required")
    private String policyVersion;

    private boolean accepted;

    public PrivacyConsentRequest() {
    }

    public PrivacyConsentRequest(String policyVersion, boolean accepted) {
        this.policyVersion = policyVersion;
        this.accepted = accepted;
    }

    public String getPolicyVersion() {
        return policyVersion;
    }

    public void setPolicyVersion(String policyVersion) {
        this.policyVersion = policyVersion;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }
}

