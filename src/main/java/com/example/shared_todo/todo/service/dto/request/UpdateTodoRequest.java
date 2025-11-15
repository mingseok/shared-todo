package com.example.shared_todo.todo.service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateTodoRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이내로 입력해 주세요.")
    private String title;

    @Size(max = 1000, message = "내용은 1000자 이내로 입력해 주세요.")
    private String content;

    private LocalDate dueDate;

    private Boolean completed;
}
