package com.example.shared_todo.share.entity;

import com.example.shared_todo.common.BaseEntity;
import com.example.shared_todo.todo.entity.Todo;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Share extends BaseEntity {

    @Column(name = "share_token", nullable = false, unique = true, length = 36)
    private String shareToken;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(name = "shared_by", nullable = false)
    private Long sharedBy;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Builder
    private Share(String shareToken, Todo todo, Long sharedBy, LocalDateTime expiresAt) {
        this.shareToken = shareToken;
        this.todo = todo;
        this.sharedBy = sharedBy;
        this.expiresAt = expiresAt;
    }

    public static Share create(Todo todo, Long sharedBy) {
        return Share.builder()
                .shareToken(UUID.randomUUID().toString())
                .todo(todo)
                .sharedBy(sharedBy)
                .build();
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
