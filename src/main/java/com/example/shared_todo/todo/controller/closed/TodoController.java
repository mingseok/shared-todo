package com.example.shared_todo.todo.controller.closed;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.common.identity.annotation.AuthMemberId;
import com.example.shared_todo.todo.service.TodoService;
import com.example.shared_todo.todo.service.dto.request.CreateTodoRequest;
import com.example.shared_todo.todo.service.dto.request.UpdateTodoRequest;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public ResponseEntity<ApiResponse<List<TodoResponse>>> findAll(
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) Boolean completed,
            @RequestParam(required = false) Long tagId
    ) {
        List<TodoResponse> responses = todoService.findTodos(memberId, completed, tagId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> findById(
            @PathVariable Long id
    ) {
        TodoResponse response = todoService.findTodoById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoResponse>> update(
            @Validated @RequestBody UpdateTodoRequest request,
            @PathVariable Long id,
            @AuthMemberId Long memberId
    ) {
        TodoResponse response = todoService.updateTodo(id, request, memberId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthMemberId Long memberId
    ) {
        todoService.deleteTodo(id, memberId);
        return ResponseEntity.ok(ApiResponse.successEmpty());
    }
}
