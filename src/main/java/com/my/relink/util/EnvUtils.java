package com.my.relink.util;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvUtils {
    private static final Dotenv dotenv = Dotenv.configure()
            .directory("/home/ubuntu") // .env 파일 위치 설정
            .load();

    public static String get(String key) {
        return dotenv.get(key);
    }
}
