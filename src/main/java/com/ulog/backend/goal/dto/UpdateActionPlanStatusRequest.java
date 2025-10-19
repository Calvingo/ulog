package com.ulog.backend.goal.dto;

import com.ulog.backend.domain.goal.enums.ActionPlanStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class UpdateActionPlanStatusRequest {

    @NotNull(message = "status is required")
    private ActionPlanStatus status;

    private LocalDateTime completedAt;

    public ActionPlanStatus getStatus() {
        return status;
    }

    public void setStatus(ActionPlanStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}

