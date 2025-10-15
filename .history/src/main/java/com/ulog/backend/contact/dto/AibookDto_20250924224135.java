package com.ulog.backend.contact.dto;

import java.util.List;

public class AibookDto {
    
    public static class Item {
        private String point;   // 要点
        private String reason;  // 来由/引用（简述）
        private String source;  // "contact" | "user" | "both"

        public Item() {}

        public Item(String point, String reason, String source) {
            this.point = point;
            this.reason = reason;
            this.source = source;
        }

        public String getPoint() { return point; }
        public void setPoint(String point) { this.point = point; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
        public String getSource() { return source; }
        public void setSource(String source) { this.source = source; }
    }
    
    private List<Item> behaviorTendencies;          // 行为倾向推断
    private List<Item> valuesAndEmotions;           // 情绪与价值观暗示
    private List<Item> latentNeeds;                 // 潜在需求推理
    private List<Item> taboos;                      // 可能的禁忌雷区
    private List<Item> relationshipOpportunities;   // 人际关系机会点

    public AibookDto() {}

    public List<Item> getBehaviorTendencies() { return behaviorTendencies; }
    public void setBehaviorTendencies(List<Item> behaviorTendencies) { this.behaviorTendencies = behaviorTendencies; }
    
    public List<Item> getValuesAndEmotions() { return valuesAndEmotions; }
    public void setValuesAndEmotions(List<Item> valuesAndEmotions) { this.valuesAndEmotions = valuesAndEmotions; }
    
    public List<Item> getLatentNeeds() { return latentNeeds; }
    public void setLatentNeeds(List<Item> latentNeeds) { this.latentNeeds = latentNeeds; }
    
    public List<Item> getTaboos() { return taboos; }
    public void setTaboos(List<Item> taboos) { this.taboos = taboos; }
    
    public List<Item> getRelationshipOpportunities() { return relationshipOpportunities; }
    public void setRelationshipOpportunities(List<Item> relationshipOpportunities) { this.relationshipOpportunities = relationshipOpportunities; }
}
