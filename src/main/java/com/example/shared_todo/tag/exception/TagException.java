package com.example.shared_todo.tag.exception;

import com.example.shared_todo.common.exception.CommonError;
import com.example.shared_todo.common.exception.ServiceException;

public class TagException extends ServiceException {

    public TagException(CommonError commonError) {
        super(commonError);
    }
}
