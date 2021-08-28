package com.tribal.challenge.config.exceptions;

import com.tribal.challenge.models.enums.ErrorCode;

public class BusinessExceptions extends RuntimeException{

    private final String message;
    private final ErrorCode errorCode;

    public BusinessExceptions(ErrorCode errorCode, String message){
        this.errorCode = errorCode;
        this.message = message;
    }

    @Override
    public String getMessage(){ return this.message; }

    public ErrorCode getErrorCode(){
        return this.errorCode;
    }
}
