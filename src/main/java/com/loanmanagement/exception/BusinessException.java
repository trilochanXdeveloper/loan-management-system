package com.loanmanagement.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends CustomException{

    public BusinessException(String message){
        super(message,HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, HttpStatus status) {
        super(message, status);
    }
}
