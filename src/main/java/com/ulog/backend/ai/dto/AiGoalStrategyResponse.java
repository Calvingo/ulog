package com.ulog.backend.ai.dto;

import java.util.List;

public class AiGoalStrategyResponse {

    private String strategy;
    private List<AiActionPlanItem> actionPlans;

    public AiGoalStrategyResponse() {
    }

    public AiGoalStrategyResponse(String strategy, List<AiActionPlanItem> actionPlans) {
        this.strategy = strategy;
        this.actionPlans = actionPlans;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public List<AiActionPlanItem> getActionPlans() {
        return actionPlans;
    }

    public void setActionPlans(List<AiActionPlanItem> actionPlans) {
        this.actionPlans = actionPlans;
    }
}

