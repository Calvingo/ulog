package com.ulog.backend.conversation.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    
    private String sessionId;
    private Long contactId;
    private String contactName;
    private String status;
    private Integer progress;
    private LocalDateTime createdAt;
    private LocalDateTime lastActiveAt;
}
