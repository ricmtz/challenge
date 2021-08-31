package com.tribal.challenge.config.exceptions;

import com.tribal.challenge.models.enums.ErrorCode;

public class BusinessException extends RuntimeException{

    private final String message;
    private final ErrorCode errorCode;

    public static final BusinessException MAX_ATTEMPTS_EXCEEDED = new BusinessException(ErrorCode.REJECTED,
            "A sales person will contact you");

    public BusinessException(ErrorCode errorCode, String message){
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage(){ return this.message; }

    public ErrorCode getErrorCode(){
        return this.errorCode;
    }
}
