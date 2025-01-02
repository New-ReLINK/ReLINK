package com.my.relink.config;

import com.my.relink.util.EnvUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public String jwtSecretKey() {
        return EnvUtils.get("JWT_SECRET_KEY");
    }
}
