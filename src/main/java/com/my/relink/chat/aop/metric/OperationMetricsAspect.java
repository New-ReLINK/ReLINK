package com.my.relink.chat.aop.metric;

import com.my.relink.chat.aop.metric.OperationMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class OperationMetricsAspect {

    private final OperationMetrics metrics;

    @Around("@annotation(TimeMetric)")
    public Object measureTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String operationName = joinPoint.getSignature().getName();
        long startTime = System.currentTimeMillis();

        try{
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis() - startTime;
            metrics.recordTime(operationName, endTime);
        }
    }
}
