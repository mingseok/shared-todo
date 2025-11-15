package com.example.shared_todo.todo.controller.closed;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.common.identity.annotation.AuthMemberId;
import com.example.shared_todo.todo.service.TodoService;
import com.example.shared_todo.todo.service.dto.request.CreateTodoRequest;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos")
public class TodoController {

    private final TodoService todoService;

    @PostMapping
    public ResponseEntity<ApiResponse<TodoResponse>> create(
            @Validated @RequestBody CreateTodoRequest request,
            @AuthMemberId Long memberId
    ) {
        TodoResponse response = todoService.createTodo(request, memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }
}
