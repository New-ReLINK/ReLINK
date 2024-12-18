package com.my.relink.config.security;

import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.ex.ErrorCode;
import com.my.relink.ex.SecurityFilterChainException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (isTestEndpoint(request)|| request.getRequestURI().contains("/auth") || isWsEndPoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String tokenValue = request.getHeader(JwtProvider.AUTHENTICATION_HEADER_PREFIX);
        if(tokenValue == null){
            throw new SecurityFilterChainException(ErrorCode.TOKEN_NOT_FOUND);
        }
        String token = tokenValue.replace(JwtProvider.TOKEN_PREFIX, "");
        jwtProvider.validateToken(token);

        AuthUser authUser = jwtProvider.getAuthUserForToken(token);

        LoginAuthentication loginAuthentication = new LoginAuthentication(authUser);
        SecurityContextHolder.getContext().setAuthentication(loginAuthentication);
        filterChain.doFilter(request, response);
    }
    private boolean isWsEndPoint(HttpServletRequest request){
        return request.getRequestURI().contains("/chats");
    }

    private boolean isTestEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.contains("charge") || uri.contains("/favicon.ico") ||uri.contains("payment");
    }
}
