package com.my.relink.config.sentry;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

public class SentryExceptionResolver implements HandlerExceptionResolver, Ordered {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
                                         Object handler, Exception ex) {
        Sentry.configureScope(scope -> {
            scope.setTag("url", request.getRequestURI());
            scope.setTag("method", request.getMethod());
            scope.setTag("path", request.getRequestURI());
            scope.setExtra("query", request.getQueryString());
        });
        Sentry.captureException(ex);

        // null = run other HandlerExceptionResolvers to actually handle the exception
        return null;
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}