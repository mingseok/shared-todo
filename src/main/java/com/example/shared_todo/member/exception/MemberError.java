package com.example.shared_todo.member.exception;

import com.example.shared_todo.common.exception.CommonError;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberError implements CommonError {

    EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "40001", "이미 등록된 이메일입니다."),
    EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "40402", "존재하지 않는 회원 이메일입니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "40002", "비밀번호가 일치하지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "40403", "존재하지 않는 회원입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
