package com.my.relink;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class ReLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReLinkApplication.class, args);
    }

}
