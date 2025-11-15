package com.example.shared_todo.share.exception;

import com.example.shared_todo.common.exception.CommonError;
import com.example.shared_todo.common.exception.ServiceException;

public class ShareException extends ServiceException {

    public ShareException(CommonError commonError) {
        super(commonError);
    }
}
