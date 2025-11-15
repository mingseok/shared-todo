package com.example.shared_todo.todo.controller.open;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.share.service.ShareService;
import com.example.shared_todo.todo.service.dto.response.TodoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/public/todos/share")
public class TodoPublicController {

    private final ShareService shareService;

    @GetMapping("/{shareToken}")
    public ResponseEntity<ApiResponse<TodoResponse>> findSharedTodo(
            @PathVariable String shareToken
    ) {
        TodoResponse response = shareService.findSharedTodo(shareToken);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
