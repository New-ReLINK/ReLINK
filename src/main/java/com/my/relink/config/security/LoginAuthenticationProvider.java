package com.my.relink.config.security;

import com.my.relink.domain.user.User;
import com.my.relink.domain.user.repository.UserRepository;
import com.my.relink.ex.ErrorCode;
import com.my.relink.ex.SecurityFilterChainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        LoginAuthentication loginAuthentication = (LoginAuthentication) authentication;
        String email = loginAuthentication.getName();
        String password = (String) loginAuthentication.getCredentials();

        User user = userRepository.findByEmailActiveUser(email)
                .orElseThrow(() -> new SecurityFilterChainException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new SecurityFilterChainException(ErrorCode.MISS_MATCHER_PASSWORD);
        }

        return new LoginAuthentication(AuthUser.from(user));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return LoginAuthentication.class.isAssignableFrom(authentication);
    }
}
