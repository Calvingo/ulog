package com.ulog.backend.compliance.dto;

public class ModerationResult {

    private boolean passed;
    private String result; // pass, reject, review
    private String riskLevel; // low, medium, high
    private String riskDetails;
    private String provider;

    public ModerationResult() {
    }

    public ModerationResult(boolean passed, String result, String riskLevel, String riskDetails, String provider) {
        this.passed = passed;
        this.result = result;
        this.riskLevel = riskLevel;
        this.riskDetails = riskDetails;
        this.provider = provider;
    }

    public static ModerationResult pass(String provider) {
        return new ModerationResult(true, "pass", "low", null, provider);
    }

    public static ModerationResult reject(String riskLevel, String riskDetails, String provider) {
        return new ModerationResult(false, "reject", riskLevel, riskDetails, provider);
    }

    public static ModerationResult review(String riskLevel, String riskDetails, String provider) {
        return new ModerationResult(false, "review", riskLevel, riskDetails, provider);
    }

    // Getters and Setters
    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public String getRiskDetails() {
        return riskDetails;
    }

    public void setRiskDetails(String riskDetails) {
        this.riskDetails = riskDetails;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}

