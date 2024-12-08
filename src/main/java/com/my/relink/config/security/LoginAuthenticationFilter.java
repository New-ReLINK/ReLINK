package com.my.relink.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.my.relink.config.security.dto.LoginRepDto;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.ex.ErrorCode;
import com.my.relink.ex.SecurityFilterChainException;
import com.my.relink.util.api.ApiResult;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

public class LoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;

    public LoginAuthenticationFilter(AuthenticationManager authenticationManager, ObjectMapper objectMapper, JwtProvider jwtProvider) {
        super(new AntPathRequestMatcher("/auth/login", HttpMethod.POST.name()));
        this.setAuthenticationManager(authenticationManager);
        this.objectMapper = objectMapper;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            LoginRepDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginRepDto.class);

            LoginAuthentication loginAuthentication = new LoginAuthentication(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );
            return this.getAuthenticationManager().authenticate(loginAuthentication);
        } catch (IOException ex) {
            throw new SecurityFilterChainException(ErrorCode.JSON_PARSE_ERROR, ex);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        String token = jwtProvider.generateToken(authResult);
        response.addHeader("Authorization", token);
        response.setStatus(HttpStatus.OK.value());
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.success(null)));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.getWriter().write(objectMapper.writeValueAsString(ApiResult.error(ErrorCode.INVALID_CREDENTIALS)));
    }
}