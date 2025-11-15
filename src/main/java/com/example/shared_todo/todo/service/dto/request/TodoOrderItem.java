package com.example.shared_todo.todo.service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TodoOrderItem {

    private Long todoId;
    private Integer displayOrder;
}
