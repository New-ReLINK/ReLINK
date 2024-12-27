package com.my.relink.controller.sentry;

import io.sentry.Sentry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentryController {
    private static final Logger logger = LoggerFactory.getLogger(SentryController.class);

    @GetMapping("/trigger-error")
    public String triggerError() {
        try{
            throw new RuntimeException("error to Sentry");
        }catch (Exception e) {
            logger.error(e.getMessage());
            Sentry.captureException(e);
            return e.getMessage();
        }
    }
}
