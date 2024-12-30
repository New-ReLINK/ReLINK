package com.my.relink.config.sentry;

import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import io.sentry.Sentry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.util.Objects;

@Configuration
public class SentryConfig {

    @Bean
    public HandlerExceptionResolver resolveSentryException() {
        Sentry.init(options -> {
            options.setDsn("https://fb238d0052d3ac49c7dc60a76e07846f@o4508520721940480.ingest.us.sentry.io/4508520729346048");

            options.setBeforeSend((event, hint)->{
                Throwable throwable = event.getThrowable();
                if(throwable instanceof BusinessException businessException){
                    ErrorCode errorCode = businessException.getErrorCode();
                    event.setTag("errorStatus", String.valueOf(errorCode.getStatus()));
                    event.setTag("message", errorCode.getMessage());
                }

                event.setTag("appVersion", "1.0.0");
                event.setTag("environment", Objects.requireNonNull(options.getEnvironment()));

                return event;
            });
        });
        return new SentryExceptionResolver();
    }
}
