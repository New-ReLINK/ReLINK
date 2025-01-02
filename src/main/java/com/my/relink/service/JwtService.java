package com.my.relink.service;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final String jwtSecretKey;

    public JwtService(String jwtSecretKey) {
        this.jwtSecretKey = jwtSecretKey;
    }

    public String getSecretKey() {
        return jwtSecretKey;
    }
}
