package com.my.relink.chat.config.metric;

import org.springframework.stereotype.Component;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class WebSocketMetricsHolder {

    private final Queue<TaskMetric> recentTaskMetrics = new ConcurrentLinkedQueue<>();
    private static final int MAX_METRICS_SIZE = 1000;

    public void addMetric(TaskMetric metric) {
        recentTaskMetrics.offer(metric);
        while (recentTaskMetrics.size() > MAX_METRICS_SIZE) { //큐 크기가 MAX_METRICS_SIZE 초과하면 오래된 메트릭 제거
            recentTaskMetrics.poll();
        }
    }

    public Queue<TaskMetric> getRecentMetrics() {
        return recentTaskMetrics;
    }
}
