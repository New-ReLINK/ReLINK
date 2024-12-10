package com.my.relink.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateTimeUtil {

    private static final DateTimeFormatter TODAY_MESSAGE_FORMAT =
            DateTimeFormatter.ofPattern("a h:m").withLocale(Locale.KOREA);
    private static final DateTimeFormatter THIS_YEAR_MESSAGE_FORMAT =
            DateTimeFormatter.ofPattern("M월 d일 a h:m").withLocale(Locale.KOREA);
    private static final DateTimeFormatter PAST_YEAR_MESSAGE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:m").withLocale(Locale.KOREA);
    

    public static String getMessageFormattedTime(LocalDateTime messageTime, LocalDateTime now){
        if(messageTime.toLocalDate().equals(now.toLocalDate())){
            return messageTime.format(TODAY_MESSAGE_FORMAT);
        } else if (messageTime.getYear() == now.getYear()){
            return messageTime.format(THIS_YEAR_MESSAGE_FORMAT);
        } else {
            return messageTime.format(PAST_YEAR_MESSAGE_FORMAT);
        }
    }
}
