package com.example.shared_todo.share.service.dto.response;

import com.example.shared_todo.share.entity.Share;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShareResponse {

    private final Long id;
    private final String shareToken;
    private final Long todoId;
    private final Long sharedBy;
    private final LocalDateTime expiresAt;
    private final String createdAt;

    public static ShareResponse from(Share share) {
        return ShareResponse.builder()
                .id(share.getId())
                .shareToken(share.getShareToken())
                .todoId(share.getTodo().getId())
                .sharedBy(share.getSharedBy())
                .expiresAt(share.getExpiresAt())
                .createdAt(share.getCreatedAt().toString())
                .build();
    }
}
