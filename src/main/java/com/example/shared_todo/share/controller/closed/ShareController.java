package com.example.shared_todo.share.controller.closed;

import com.example.shared_todo.common.dto.ApiResponse;
import com.example.shared_todo.common.identity.annotation.AuthMemberId;
import com.example.shared_todo.share.service.ShareService;
import com.example.shared_todo.share.service.dto.response.ShareResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/todos/{todoId}/share")
public class ShareController {

    private final ShareService shareService;

    @PostMapping
    public ResponseEntity<ApiResponse<ShareResponse>> createShare(
            @PathVariable Long todoId,
            @AuthMemberId Long memberId
    ) {
        ShareResponse response = shareService.createShare(todoId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteShare(
            @PathVariable Long todoId,
            @AuthMemberId Long memberId
    ) {
        shareService.deleteShare(todoId, memberId);
        return ResponseEntity.ok(ApiResponse.successEmpty());
    }
}
