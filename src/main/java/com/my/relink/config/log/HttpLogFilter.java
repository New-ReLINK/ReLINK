package com.my.relink.config.log;

import com.my.relink.config.log.context.HttpMDCContext;
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
import java.util.Optional;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class HttpLogFilter extends OncePerRequestFilter {

    private static final String X_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestId = Optional.ofNullable(request.getHeader(X_REQUEST_ID))
                .orElse(UUID.randomUUID().toString());

        try(HttpMDCContext context = new HttpMDCContext()){
            context.putStartTime(startTime);
            context.putClientIp(request.getRemoteAddr());
            context.putUri(request.getRequestURI());
            context.putMethod(request.getMethod());
            context.putRequestId(request.getRequestId());
            context.putRequestId(requestId);
            response.setHeader(X_REQUEST_ID, requestId);

            log.info("Request received - {} {}", request.getMethod(), request.getRequestURI());

            filterChain.doFilter(request, response);

            long duration = System.currentTimeMillis() - startTime;
            context.putDuration(duration);
            context.putStatus(response.getStatus());

            log.info("Request completed");
        }
    }
}
