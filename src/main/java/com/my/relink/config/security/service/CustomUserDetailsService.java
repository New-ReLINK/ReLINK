package com.my.relink.config.security.service;

import com.my.relink.config.security.domain.CustomUserDetails;
import com.my.relink.domain.user.UserRepository;
import com.my.relink.ex.ErrorCode;
import com.my.relink.ex.SecurityFilterChainException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .map(e -> new CustomUserDetails(e.getEmail(), e.getRole().name(), e.getPassword()))
                .orElseThrow(() -> new SecurityFilterChainException(ErrorCode.USER_NOT_FOUND));
    }
}
