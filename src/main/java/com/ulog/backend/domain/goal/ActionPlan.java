package com.ulog.backend.domain.goal;

import com.ulog.backend.domain.goal.enums.ActionPlanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "action_plans")
public class ActionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goal_id", nullable = false)
    private RelationshipGoal goal;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_time", nullable = false)
    private LocalDateTime scheduledTime;

    @Column(name = "is_adopted", nullable = false)
    private Boolean isAdopted;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ActionPlanStatus status;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private Boolean deleted;

    public ActionPlan() {
        this.isAdopted = Boolean.TRUE;
        this.status = ActionPlanStatus.PENDING;
        this.deleted = Boolean.FALSE;
        this.orderIndex = 0;
    }

    public ActionPlan(RelationshipGoal goal, String title, String description, 
                      LocalDateTime scheduledTime, Integer orderIndex) {
        this.goal = goal;
        this.title = title;
        this.description = description;
        this.scheduledTime = scheduledTime;
        this.orderIndex = orderIndex;
        this.isAdopted = Boolean.TRUE;
        this.status = ActionPlanStatus.PENDING;
        this.deleted = Boolean.FALSE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public RelationshipGoal getGoal() {
        return goal;
    }

    public void setGoal(RelationshipGoal goal) {
        this.goal = goal;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public Boolean getIsAdopted() {
        return isAdopted;
    }

    public void setIsAdopted(Boolean isAdopted) {
        this.isAdopted = isAdopted;
    }

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

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return Boolean.TRUE.equals(deleted);
    }
}

