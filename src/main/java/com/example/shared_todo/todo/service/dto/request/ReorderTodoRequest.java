package com.example.shared_todo.todo.service.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReorderTodoRequest {

    @NotEmpty(message = "순서를 변경할 Todo 목록을 입력해주세요.")
    @Valid
    private List<TodoOrderItem> orders;
}
