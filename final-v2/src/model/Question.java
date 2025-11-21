package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Question {
    private String questionId;
    private String username;
    private String content;
    private LocalDateTime createdAt;

    private String email; // 用于 Admin 显示

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Question(String username, String content) {
        this.questionId = "Q-" + UUID.randomUUID();
        this.username = username;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public Question(String questionId, String username, String content, LocalDateTime createdAt) {
        this.questionId = questionId;
        this.username = username;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getQuestionId() { return questionId; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
