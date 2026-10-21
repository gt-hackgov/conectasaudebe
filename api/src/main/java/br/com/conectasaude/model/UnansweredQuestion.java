package br.com.conectasaude.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "unanswered_questions")
public class UnansweredQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String userMessage;

    @Column(name = "asked_at")
    private LocalDateTime askedAt;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean resolved;

    public UnansweredQuestion() {
        this.askedAt = LocalDateTime.now();
        this.resolved = false;
    }

    public UnansweredQuestion(String userMessage) {
        this.userMessage = userMessage;
        this.askedAt = LocalDateTime.now();
        this.resolved = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public void setUserMessage(String userMessage) {
        this.userMessage = userMessage;
    }

    public LocalDateTime getAskedAt() {
        return askedAt;
    }

    public void setAskedAt(LocalDateTime askedAt) {
        this.askedAt = askedAt;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }
}
