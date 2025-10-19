package com.ulog.backend.domain.goal;

import com.ulog.backend.domain.contact.Contact;
import com.ulog.backend.domain.goal.enums.GoalStatus;
import com.ulog.backend.domain.user.User;
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
@Table(name = "relationship_goals")
public class RelationshipGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contact_id", nullable = false)
    private Contact contact;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "goal_description", nullable = false, columnDefinition = "TEXT")
    private String goalDescription;

    @Column(name = "ai_strategy", columnDefinition = "TEXT")
    private String aiStrategy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GoalStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private Boolean deleted;

    public RelationshipGoal() {
        this.status = GoalStatus.ACTIVE;
        this.deleted = Boolean.FALSE;
    }

    public RelationshipGoal(Contact contact, User user, String goalDescription) {
        this.contact = contact;
        this.user = user;
        this.goalDescription = goalDescription;
        this.status = GoalStatus.ACTIVE;
        this.deleted = Boolean.FALSE;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Contact getContact() {
        return contact;
    }

    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getGoalDescription() {
        return goalDescription;
    }

    public void setGoalDescription(String goalDescription) {
        this.goalDescription = goalDescription;
    }

    public String getAiStrategy() {
        return aiStrategy;
    }

    public void setAiStrategy(String aiStrategy) {
        this.aiStrategy = aiStrategy;
    }

    public GoalStatus getStatus() {
        return status;
    }

    public void setStatus(GoalStatus status) {
        this.status = status;
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

