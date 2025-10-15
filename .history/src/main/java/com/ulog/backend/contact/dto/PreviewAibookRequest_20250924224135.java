package com.ulog.backend.contact.dto;

import jakarta.validation.constraints.Pattern;

public class PreviewAibookRequest {
    private String contactDescriptionOverride;  // 可选：覆盖联系人描述
    private String userDescriptionOverride;     // 可选：覆盖当前用户描述
    
    @Pattern(regexp = "zh|en", message = "language must be 'zh' or 'en'")
    private String language = "zh";            // zh 或 en

    public PreviewAibookRequest() {}

    public String getContactDescriptionOverride() { return contactDescriptionOverride; }
    public void setContactDescriptionOverride(String contactDescriptionOverride) { this.contactDescriptionOverride = contactDescriptionOverride; }
    
    public String getUserDescriptionOverride() { return userDescriptionOverride; }
    public void setUserDescriptionOverride(String userDescriptionOverride) { this.userDescriptionOverride = userDescriptionOverride; }
    
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
