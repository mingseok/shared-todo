package com.example.shared_todo.share.exception;

import com.example.shared_todo.common.exception.CommonError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ShareError implements CommonError {

    SHARE_NOT_FOUND(HttpStatus.NOT_FOUND, "41001", "존재하지 않는 공유입니다."),
    SHARE_EXPIRED(HttpStatus.GONE, "41002", "만료된 공유 링크입니다."),
    ALREADY_SHARED(HttpStatus.BAD_REQUEST, "40007", "이미 공유된 Todo입니다."),
    FORBIDDEN_SHARE_DELETE(HttpStatus.FORBIDDEN, "40308", "공유를 삭제할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
