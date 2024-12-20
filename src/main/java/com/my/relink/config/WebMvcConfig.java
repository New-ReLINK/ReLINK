package com.my.relink.config;

import com.my.relink.config.log.HttpLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final HttpLoggingInterceptor httpLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry
                .addInterceptor(httpLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/health",
                        "/favicon.ico",
                        "/actuator/**"
                );
    }
}
