package com.ulog.backend.goal.dto;

import com.ulog.backend.domain.goal.enums.DeviceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RegisterPushTokenRequest {

    @NotBlank(message = "deviceToken is required")
    private String deviceToken;

    @NotNull(message = "deviceType is required")
    private DeviceType deviceType;

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }
}

