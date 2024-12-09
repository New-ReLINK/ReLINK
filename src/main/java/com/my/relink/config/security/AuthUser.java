package com.my.relink.config.security;

import com.my.relink.domain.user.Role;
import com.my.relink.domain.user.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@Getter
public class AuthUser {

    private final String email;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthUser(String email, Role role) {
        this.email = email;
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    public User toUser(){
        return User.builder()
                .email(email)
                .build();
    }

    public static AuthUser from(User user) {
        return new AuthUser(user.getEmail(), user.getRole());
    }
}
