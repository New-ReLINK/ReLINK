package com.my.relink.config.log;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class HttpLogFilter extends OncePerRequestFilter {

    private static final String START_TIME = "startTime";
    private static final String REQUEST_ID = "requestId";
    private static final String METHOD = "method";
    private static final String URI = "uri";
    private static final String STATUS = "status";
    private static final String DURATION = "duration";

    private static final String CLIENT_IP = "clientIp";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();

        try {
            MDC.put(START_TIME, String.valueOf(startTime));
            MDC.put(REQUEST_ID, UUID.randomUUID().toString());
            MDC.put(METHOD, request.getMethod());
            MDC.put(URI, request.getRequestURI());
            MDC.put(CLIENT_IP, request.getRemoteAddr());

            log.info("Request received - {} {}", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;

            MDC.put(STATUS, String.valueOf(response.getStatus()));
            MDC.put(DURATION, String.valueOf(duration));

            log.info("Request completed - Status: {}, Duration: {}ms",
                    response.getStatus(),
                    duration);
        } finally {
            MDC.clear();
        }
    }
}
