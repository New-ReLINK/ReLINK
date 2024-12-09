package com.my.relink.util.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiError {
    private final String msg;
    private final int status;

    public ApiError(String msg, int status) {
        this.msg = msg;
        this.status = status;
    }

    private final String message;
    private final int status;

}
