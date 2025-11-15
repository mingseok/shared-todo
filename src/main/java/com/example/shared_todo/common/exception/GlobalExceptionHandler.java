package com.example.shared_todo.common.exception;

import com.example.shared_todo.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleServiceException(ServiceException ex) {
        CommonError commonError = ex.getCommonError();
        log.warn("ServiceException 발생: {}", commonError.getMessage());
        return new ResponseEntity<>(
                ApiResponse.failure(commonError.getMessage()),
                commonError.getHttpStatus()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String errorMessage = ErrorCode.BAD_REQUEST.getMessage();
        if (fieldError != null) {
            errorMessage = fieldError.getDefaultMessage();
        }

        log.warn("MethodArgumentNotValidException 발생: {}", errorMessage);
        return new ResponseEntity<>(
                ApiResponse.failure(errorMessage),
                ErrorCode.BAD_REQUEST.getHttpStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception ex) {
        log.error("Unhandled Exception", ex);
        return new ResponseEntity<>(
                ApiResponse.failure("서버 오류가 발생했습니다."),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
