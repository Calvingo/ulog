package com.ulog.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate.limit")
public class RateLimitProperties {

    private boolean enabled = true;
    private int defaultPerMinute = 60;
    private int loginPerMinute = 5;
    private int aiPerMinute = 60;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getDefaultPerMinute() {
        return defaultPerMinute;
    }

    public void setDefaultPerMinute(int defaultPerMinute) {
        this.defaultPerMinute = defaultPerMinute;
    }

    public int getLoginPerMinute() {
        return loginPerMinute;
    }

    public void setLoginPerMinute(int loginPerMinute) {
        this.loginPerMinute = loginPerMinute;
    }

    public int getAiPerMinute() {
        return aiPerMinute;
    }

    public void setAiPerMinute(int aiPerMinute) {
        this.aiPerMinute = aiPerMinute;
    }
}

