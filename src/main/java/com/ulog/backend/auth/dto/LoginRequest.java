package com.ulog.backend.auth.dto;

import com.ulog.backend.util.E164Phone;
import jakarta.validation.constraints.NotBlank;

public class LoginRequest {

    @E164Phone
    @NotBlank(message = "phone is required")
    private String phone;

    @NotBlank(message = "password is required")
    private String password;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
