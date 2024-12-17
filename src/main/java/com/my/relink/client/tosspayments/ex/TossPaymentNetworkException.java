package com.my.relink.client.tosspayments.ex;

import org.springframework.web.client.RestClientException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

public class TossPaymentNetworkException extends RuntimeException{
    public TossPaymentNetworkException(String message, Throwable cause) {
        super(message, cause);
    }
    public static boolean isRetryableException(Exception e) {
        return e instanceof ConnectException ||
                e instanceof SocketTimeoutException ||
                e instanceof IOException;
    }
}
