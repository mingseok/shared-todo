package com.example.shared_todo.tag.exception;

import com.example.shared_todo.common.exception.CommonError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TagError implements CommonError {

    TAG_NOT_FOUND(HttpStatus.NOT_FOUND, "40901", "존재하지 않는 태그입니다."),
    TAG_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "40006", "이미 존재하는 태그 이름입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
