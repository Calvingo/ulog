package com.ulog.backend.auth.dto;

import com.ulog.backend.util.E164Phone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @E164Phone
    @NotBlank(message = "phone is required")
    private String phone;

    @NotBlank(message = "password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[A-Za-z\\d]{8,64}$", message = "password must be 8-64 chars with upper, lower, digit")
    private String password;

    @NotBlank(message = "name is required")
    @Size(min = 1, max = 64, message = "name length must be 1-64")
    private String name;

    @NotBlank(message = "smsCode is required")
    private String smsCode;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }
}
