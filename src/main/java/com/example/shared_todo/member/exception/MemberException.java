package com.example.shared_todo.member.exception;

import com.example.shared_todo.common.exception.CommonError;
import com.example.shared_todo.common.exception.ServiceException;

public class MemberException extends ServiceException {

    public MemberException(CommonError commonError) {
        super(commonError);
    }
}
