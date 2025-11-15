package com.example.shared_todo.todo.exception;

import com.example.shared_todo.common.exception.CommonError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum TodoError implements CommonError {

    TODO_NOT_FOUND(HttpStatus.NOT_FOUND, "40401", "존재하지 않는 할 일입니다."),
    FORBIDDEN_TODO_ACCESS(HttpStatus.FORBIDDEN, "40301", "해당 할 일에 대한 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
