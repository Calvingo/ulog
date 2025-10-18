package com.ulog.backend.conversation.dto;

import java.util.Arrays;
import java.util.List;

/**
 * 自我价值评分DTO
 * 格式：4.5,3.5,2.5,4.0,3.25
 * 对应：[自尊, 自我接纳, 自我效能, 存在价值感, 自我一致性]
 */
public class SelfValue {
    
    private Double selfEsteem;        // 自尊 (1.0-5.0)
    private Double selfAcceptance;    // 自我接纳 (1.0-5.0)
    private Double selfEfficacy;      // 自我效能 (1.0-5.0)
    private Double existentialValue;  // 存在价值感 (1.0-5.0)
    private Double selfConsistency;   // 自我一致性 (1.0-5.0)
    
    public SelfValue() {
    }
    
    public SelfValue(Double selfEsteem, Double selfAcceptance, Double selfEfficacy, 
                     Double existentialValue, Double selfConsistency) {
        this.selfEsteem = selfEsteem;
        this.selfAcceptance = selfAcceptance;
        this.selfEfficacy = selfEfficacy;
        this.existentialValue = existentialValue;
        this.selfConsistency = selfConsistency;
    }
    
    /**
     * 解析字符串格式的自我价值评分
     * @param selfValueStr 格式：4.5,3.5,2.5,4.0,3.25
     * @return SelfValue对象
     */
    public static SelfValue parse(String selfValueStr) {
        if (selfValueStr == null || selfValueStr.trim().isEmpty()) {
            return getDefaultSelfValue();
        }
        
        try {
            String[] values = selfValueStr.split(",");
            if (values.length != 5) {
                return getDefaultSelfValue();
            }
            
            return new SelfValue(
                Double.parseDouble(values[0].trim()),
                Double.parseDouble(values[1].trim()),
                Double.parseDouble(values[2].trim()),
                Double.parseDouble(values[3].trim()),
                Double.parseDouble(values[4].trim())
            );
        } catch (NumberFormatException e) {
            return getDefaultSelfValue();
        }
    }
    
    /**
     * 格式化为字符串
     * @param selfValue SelfValue对象
     * @return 格式化的字符串
     */
    public static String format(SelfValue selfValue) {
        if (selfValue == null) {
            return getDefaultSelfValue().toString();
        }
        return selfValue.toString();
    }
    
    /**
     * 获取默认自我价值评分（中等水平）
     */
    public static SelfValue getDefaultSelfValue() {
        return new SelfValue(3.0, 3.0, 3.0, 3.0, 3.0);
    }
    
    /**
     * 验证评分是否在有效范围内
     */
    public boolean isValid() {
        List<Double> values = Arrays.asList(selfEsteem, selfAcceptance, selfEfficacy, existentialValue, selfConsistency);
        return values.stream().allMatch(value -> value != null && value >= 1.0 && value <= 5.0);
    }
    
    @Override
    public String toString() {
        return String.format("%.1f,%.1f,%.1f,%.1f,%.1f", 
            selfEsteem, selfAcceptance, selfEfficacy, existentialValue, selfConsistency);
    }
    
    // Getters and Setters
    public Double getSelfEsteem() {
        return selfEsteem;
    }
    
    public void setSelfEsteem(Double selfEsteem) {
        this.selfEsteem = selfEsteem;
    }
    
    public Double getSelfAcceptance() {
        return selfAcceptance;
    }
    
    public void setSelfAcceptance(Double selfAcceptance) {
        this.selfAcceptance = selfAcceptance;
    }
    
    public Double getSelfEfficacy() {
        return selfEfficacy;
    }
    
    public void setSelfEfficacy(Double selfEfficacy) {
        this.selfEfficacy = selfEfficacy;
    }
    
    public Double getExistentialValue() {
        return existentialValue;
    }
    
    public void setExistentialValue(Double existentialValue) {
        this.existentialValue = existentialValue;
    }
    
    public Double getSelfConsistency() {
        return selfConsistency;
    }
    
    public void setSelfConsistency(Double selfConsistency) {
        this.selfConsistency = selfConsistency;
    }
}
