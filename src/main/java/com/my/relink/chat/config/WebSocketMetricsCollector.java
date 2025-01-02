package com.my.relink.chat.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadPoolExecutor;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketMetricsCollector {
    private final WebSocketSessionManager sessionManager;

    private final ThreadPoolTaskExecutor webSocketTaskExecutor;
    private long lastCompletedTasks = 0;


    //@Scheduled(fixedRate = 1000)
    public void logMetrics() {
        ThreadPoolExecutor tpe = webSocketTaskExecutor.getThreadPoolExecutor();
        long completedTasks = tpe.getCompletedTaskCount();

        // 초당 처리량 계산
        long tasksDelta = completedTasks - lastCompletedTasks;
        lastCompletedTasks = completedTasks;

        log.info("WebSocket Metrics - " +
                        "활성 세션: {}, " +
                        "활성 스레드: {}, " +
                        "완료된 작업: {}, " +
                        "큐 크기: {}, " +
                        "큐 남은 용량: {}," +
                        "초당 처리량: {}",
                sessionManager.getActiveSessionCount(),
                webSocketTaskExecutor.getActiveCount(),
                completedTasks,
                webSocketTaskExecutor.getQueueSize(),
                tpe.getQueue().remainingCapacity(),
                tasksDelta);
    }
}