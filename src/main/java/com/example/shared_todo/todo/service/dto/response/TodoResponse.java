package com.example.shared_todo.todo.service.dto.response;

import com.example.shared_todo.tag.service.dto.response.TagResponse;
import com.example.shared_todo.todo.entity.Todo;
import com.example.shared_todo.todotag.entity.TodoTag;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Getter
@Builder
public class TodoResponse {

    private final Long id;
    private final String title;
    private final String content;
    private final LocalDate dueDate;
    private final Long dDay;
    private final Long memberId;
    private final boolean completed;
    private final List<TagResponse> tags;
    private final String createdAt;
    private final String updatedAt;

    public static TodoResponse from(Todo todo, List<TodoTag> todoTags) {
        return TodoResponse.builder()
                .id(todo.getId())
                .title(todo.getTitle())
                .content(todo.getContent())
                .dueDate(todo.getDueDate())
                .dDay(calculateDDay(todo.getDueDate()))
                .memberId(todo.getMemberId())
                .completed(todo.isCompleted())
                .tags(todoTags.stream()
                        .map(todoTag -> TagResponse.from(todoTag.getTag()))
                        .toList())
                .createdAt(todo.getCreatedAt().toString())
                .updatedAt(todo.getUpdatedAt().toString())
                .build();
    }

    private static Long calculateDDay(LocalDate dueDate) {
        if (dueDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
    }
}
