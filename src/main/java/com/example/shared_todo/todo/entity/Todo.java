package com.example.shared_todo.todo.entity;

import com.example.shared_todo.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseEntity {

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Builder
    public Todo(String title, String content, LocalDate dueDate, Long memberId) {
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        this.memberId = memberId;
        this.completed = false;
    }

    public static Todo create(String title, String content, LocalDate dueDate, Long memberId) {
        return Todo.builder()
                .title(title)
                .content(content)
                .dueDate(dueDate)
                .memberId(memberId)
                .build();
    }

    public void update(String title, String content, LocalDate dueDate, Boolean completed) {
        this.title = title;
        this.content = content;
        this.dueDate = dueDate;
        if (completed != null) {
            this.completed = completed;
        }
    }

    public boolean isOwner(Long memberId) {
        return this.memberId.equals(memberId);
    }
}
