package com.my.relink.config.sentry;

import io.sentry.Sentry;
import io.sentry.SentryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class SentryConfig {

    @Bean
    public HandlerExceptionResolver resolveSentryException() {
        Sentry.init(options -> {
            options.setDsn("https://fb238d0052d3ac49c7dc60a76e07846f@o4508520721940480.ingest.us.sentry.io/4508520729346048");

            // SentryOptions.Proxy를 사용하여 프록시 설정
//            SentryOptions.Proxy proxy = new SentryOptions.Proxy();
//            proxy.setHost("");
//            proxy.setPort("");
//            options.setProxy(proxy);
        });
        return new SentryExceptionResolver();
    }
}
