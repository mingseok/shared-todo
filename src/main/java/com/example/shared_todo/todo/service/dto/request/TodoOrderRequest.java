package com.example.shared_todo.todo.service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoOrderRequest {

    @NotNull(message = "Todo ID를 입력해주세요.")
    private Long todoId;

    @NotNull(message = "순서를 입력해주세요.")
    private Integer displayOrder;
}
