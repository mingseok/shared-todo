package com.example.shared_todo.common.identity.exception;

import com.example.shared_todo.common.exception.CommonError;
import com.example.shared_todo.common.exception.ServiceException;

public class CommentException extends ServiceException {

    public CommentException(CommonError commonError) {
        super(commonError);
    }
}
