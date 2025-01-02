package com.my.relink.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.relink.ex.ErrorCode;
import com.my.relink.ex.SecurityFilterChainException;
import com.my.relink.util.api.ApiResult;
import io.sentry.Sentry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
public class GlobalFilterExceptionHandler extends OncePerRequestFilter {

    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (SecurityFilterChainException e) {
            log.info("인증 / 인가 필터 예외 : {}", e.getMessage());
            // Sentry로 예외 전송
            Sentry.captureException(e);
            errorResponse(response, ErrorCode.AUTH_FAIL_ERROR);
        } catch (Exception e) {
            log.info("서버 측 에러 : {}", e.getMessage());
            // Sentry로 예외 전송
            Sentry.configureScope(scope -> {
                scope.setTag("alertType", "SERVER_ERROR");
                scope.setExtra("errorMessage", e.getMessage());
            });
            Sentry.captureException(e);
            errorResponse(response, ErrorCode.FILTER_CHAIN_ERROR);
        }
    }

    private void errorResponse(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(mapper.writeValueAsString(ApiResult.error(code)));
    }
}
