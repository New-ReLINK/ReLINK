package com.my.relink.config.security.filter;

import com.my.relink.config.security.domain.CustomUserDetails;
import com.my.relink.config.security.jwt.JwtProvider;
import com.my.relink.ex.ErrorCode;
import com.my.relink.ex.SecurityFilterChainException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.io.IOException;

@Slf4j
public class CustomAuthorizationFilter extends BasicAuthenticationFilter {

    private final JwtProvider jwtProvider;
    private static final String AUTHORIZATION_HEADER_PREFIX = "Authorization";

    public CustomAuthorizationFilter(AuthenticationManager authenticationManager, JwtProvider jwtProvider) {
        super(authenticationManager);
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURI = request.getRequestURI();
        if (requestURI.contains("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        String tokenValue = request.getHeader(AUTHORIZATION_HEADER_PREFIX);
        if (!tokenValue.startsWith(JwtProvider.TOKEN_PREFIX)) {
            throw new SecurityFilterChainException(ErrorCode.TOKEN_NOT_FOUND);
        }

        String token = tokenValue.replace(JwtProvider.TOKEN_PREFIX, "");
        log.info("Header Token 정보 : {}", token);

        jwtProvider.validateToken(token);
        CustomUserDetails userDetails = jwtProvider.getUserDetailsForToken(token);

        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        chain.doFilter(request, response);
    }
}
