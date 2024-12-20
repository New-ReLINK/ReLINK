package com.my.relink.config.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String STATUS = "status";
    private static final String DURATION = "duration";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        MDC.put(START_TIME, String.valueOf(System.currentTimeMillis()));
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        MDC.put(METHOD, request.getMethod());
        MDC.put(URI, request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        try {
            long startTime = Long.parseLong(MDC.get(START_TIME));
            long duration = System.currentTimeMillis() - startTime;

            MDC.put(STATUS, String.valueOf(response.getStatus()));
            MDC.put(DURATION, String.valueOf(duration));
        } finally {
            MDC.clear();
        }
    }
}
