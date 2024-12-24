package com.my.relink.config.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Aspect
@Component
@Slf4j
public class SystemLogAspect {


    @Around("@within(org.springframework.stereotype.Service) || " +
            "@within(org.springframework.stereotype.Repository)")
    public Object logSystemOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        try {
            MDC.put("component", className);
            MDC.put("operation", methodName);
            MDC.put("traceId", UUID.randomUUID().toString());

            long startTime = System.currentTimeMillis();
            log.info("Started: {}.{}", className, methodName);

            Object result = joinPoint.proceed();

            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed: {}.{}, duration: {}ms", className, methodName, duration);

            return result;

        } catch (Exception e) {
            log.error("Failed: {}.{}, error: {}", className, methodName, e.getMessage(), e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
