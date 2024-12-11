package com.my.relink.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class LoginAuthentication implements Authentication {
    private final String email;
    private final String password;
    private boolean authenticated;
    private AuthUser principal;

    public LoginAuthentication(String email, String password) {
        this.email = email;
        this.password = password;
        this.authenticated = false;
    }

    public LoginAuthentication(AuthUser authUser) {
        this.email = authUser.getEmail();
        this.password = null;
        this.authenticated = true;
        this.principal = authUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return principal != null ? principal.getAuthorities() : Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return authenticated ? principal : email;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return email;
    }
}
