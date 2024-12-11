package com.my.relink.util;

import java.time.LocalDateTime;

public class DateTimeFormatterUtil {

    private static final String DATE_PATTEN = "yyyy-MM-dd";
    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern(DATE_PATTEN);

    public static String format(LocalDateTime dateTime){
        return dateTime.format(DATE_FORMATTER);
    }
}
