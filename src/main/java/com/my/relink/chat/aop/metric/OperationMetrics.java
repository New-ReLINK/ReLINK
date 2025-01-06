package com.my.relink.chat.aop.metric;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class OperationMetrics {

    private final Map<String, List<Long>> timeMetrics = new ConcurrentHashMap<>();

    public void recordTime(String operation, long timeInMs){
        timeMetrics.computeIfAbsent(operation, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(timeInMs);
    }

    public void printMetrics(){
        timeMetrics.forEach((operation, times) -> {
            synchronized (times){
                if(!times.isEmpty()) {
                    DoubleSummaryStatistics stats = times.stream()
                            .mapToDouble(Long::doubleValue)
                            .summaryStatistics();

                    List<Long> sortedTimes = new ArrayList<>(times);
                    Collections.sort(sortedTimes);

                    int index95 = (int) Math.ceil(0.95 * sortedTimes.size()) - 1;
                    long percentile95 = sortedTimes.get(index95);

                    log.info("\n==== [{}] Statistics ====", operation);
                    log.info("  Count: {}", stats.getCount());
                    log.info("  Average: {}ms", String.format("%.2f", stats.getAverage()));
                    log.info("  Max: {}ms", stats.getMax());
                    log.info("  Min: {}ms", stats.getMin());
                    log.info("  95th Percentile: {}ms", percentile95);

                    //초기화
                    times.clear();
                }
            }
        });
    }
}
