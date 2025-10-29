package com.ulog.backend.domain.contact;

import com.ulog.backend.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cid")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_uid", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;
    
    @Column(name = "self_value", length = 50)
    private String selfValue;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Column(name = "interaction_suggestions", columnDefinition = "TEXT")
    private String interactionSuggestions;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted")
    private Boolean deleted;

    public Contact() {
    }

    public Contact(User owner, String name, String description, String aiSummary) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.aiSummary = aiSummary;
        this.deleted = Boolean.FALSE;
    }

    public Contact(User owner, String name, String description) {
        this.owner = owner;
        this.name = name;
        this.description = description;
        this.aiSummary = null;
        this.deleted = Boolean.FALSE;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getSelfValue() {
        return selfValue;
    }
    
    public void setSelfValue(String selfValue) {
        this.selfValue = selfValue;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public void setAiSummary(String aiSummary) {
        this.aiSummary = aiSummary;
    }

    public String getInteractionSuggestions() {
        return interactionSuggestions;
    }

    public void setInteractionSuggestions(String interactionSuggestions) {
        this.interactionSuggestions = interactionSuggestions;
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
