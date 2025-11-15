package com.example.shared_todo.todo.controller.open;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.todo.service.TodoService;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/todos")
public class TodoPublicController {

    private final TodoService todoService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> findDetail(
            @PathVariable Long id
    ) {
        TodoResponse response = todoService.findTodoById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoResponse>>> findAll(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Boolean completed
    ) {
        List<TodoResponse> responses = todoService.findTodos(memberId, completed);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
