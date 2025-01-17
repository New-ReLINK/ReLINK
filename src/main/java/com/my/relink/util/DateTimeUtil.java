package com.my.relink.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DateTimeUtil {

    private final Clock clock;

    private static final DateTimeFormatter TODAY_MESSAGE_FORMAT =
            DateTimeFormatter.ofPattern("a h:m").withLocale(Locale.KOREA);
    private static final DateTimeFormatter THIS_YEAR_MESSAGE_FORMAT =
            DateTimeFormatter.ofPattern("M월 d일 a h:m").withLocale(Locale.KOREA);
    private static final DateTimeFormatter PAST_YEAR_MESSAGE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 a h:m").withLocale(Locale.KOREA);

    private static final DateTimeFormatter TRADE_STATUS_FORMAT =
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 HH:mm");

    private static final DateTimeFormatter EXCHANGE_START_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final DateTimeFormatter USAGE_POINT_HISTORY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String getUsagePointHistoryFormattedTime(LocalDateTime createdAt){
        return createdAt != null? createdAt.format(USAGE_POINT_HISTORY_FORMATTER) : "-";
    }

    public String getExchangeStartFormattedTime(LocalDateTime createdAt){
        return createdAt != null? createdAt.format(EXCHANGE_START_DATE_FORMAT) : "-";
    }
    

    public String getMessageFormattedTime(LocalDateTime messageTime){
        LocalDateTime now = LocalDateTime.now(clock);
        if(messageTime.toLocalDate().equals(now.toLocalDate())){
            return messageTime.format(TODAY_MESSAGE_FORMAT);
        } else if (messageTime.getYear() == now.getYear()){
            return messageTime.format(THIS_YEAR_MESSAGE_FORMAT);
        } else {
            return messageTime.format(PAST_YEAR_MESSAGE_FORMAT);
        }
    }

    public String getTradeStatusFormattedTime(LocalDateTime modifiedAt) {
        return modifiedAt != null ? modifiedAt.format(TRADE_STATUS_FORMAT) : "N/A";
    }
}
