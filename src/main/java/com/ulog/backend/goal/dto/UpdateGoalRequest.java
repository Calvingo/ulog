package com.ulog.backend.goal.dto;

import com.ulog.backend.domain.goal.enums.GoalStatus;

public class UpdateGoalRequest {

    private String goalDescription;

    private GoalStatus status;

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
    }
}

