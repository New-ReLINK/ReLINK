package com.my.relink.ex;

import lombok.Getter;

@Getter
public class SecurityFilterChainException extends RuntimeException {

    private final ErrorCode errorCode;

    public SecurityFilterChainException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
