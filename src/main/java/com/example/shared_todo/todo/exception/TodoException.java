package com.example.shared_todo.todo.exception;

import com.example.shared_todo.common.exception.CommonError;
import com.example.shared_todo.common.exception.ServiceException;

public class TodoException extends ServiceException {

    public TodoException(CommonError commonError) {
        super(commonError);
    }
}
