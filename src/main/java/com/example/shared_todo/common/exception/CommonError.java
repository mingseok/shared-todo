package com.example.shared_todo.common.exception;

import org.springframework.http.HttpStatus;

public interface CommonError {

    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();
}
