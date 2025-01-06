package com.my.relink.chat.service.cache.metric;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@Profile("local-1")
public class RedisPerformanceMetrics {
    private final Map<String, List<Long>> redisWriteLatencies = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> redisReadLatencies = new ConcurrentHashMap<>();
    private final Map<String, List<Integer>> batchSizeMetrics = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> batchProcessingTimes = new ConcurrentHashMap<>();
    private final AtomicLong totalRedisMemoryUsage = new AtomicLong(0);
    private final AtomicLong cacheHits = new AtomicLong(0);
    private final AtomicLong cacheMisses = new AtomicLong(0);

    public void recordRedisWrite(String operation, long latency) {
        redisWriteLatencies.computeIfAbsent(operation, k -> new CopyOnWriteArrayList<>()).add(latency);
    }

    public void recordRedisRead(String operation, long latency) {
        redisReadLatencies.computeIfAbsent(operation, k -> new CopyOnWriteArrayList<>()).add(latency);
    }

    public void recordBatchMetrics(String operation, int batchSize, long processingTime) {
        batchSizeMetrics.computeIfAbsent(operation, k -> new CopyOnWriteArrayList<>()).add(batchSize);
        batchProcessingTimes.computeIfAbsent(operation, k -> new CopyOnWriteArrayList<>()).add(processingTime);
    }


    public void printEnhancedMetrics() {
        log.info("\n=== Enhanced Performance Metrics ===");

        printRedisMetrics();
        printBatchMetrics();
        //printMemoryMetrics();
    }

    private void printRedisMetrics() {
        log.info("\n--- Redis Performance Metrics ---");

        // Write Operations
        redisWriteLatencies.forEach((operation, latencies) -> {
            if (!latencies.isEmpty()) {
                List<Long> sortedLatencies = new ArrayList<>(latencies);
                Collections.sort(sortedLatencies);

                double avgLatency = sortedLatencies.stream()
                        .mapToLong(l -> l)
                        .average()
                        .orElse(0);

                log.info("{} Write Operation:", operation);
                log.info("  Count: {}", latencies.size());
                log.info("  Average Latency: {}ms", avgLatency);
                log.info("  95th percentile: {}ms",
                        sortedLatencies.get((int)(sortedLatencies.size() * 0.95)));
                log.info("  99th percentile: {}ms",
                        sortedLatencies.get((int)(sortedLatencies.size() * 0.99)));
            }
        });
    }

    private void printBatchMetrics() {
        log.info("\n--- Batch Processing Metrics ---");

        batchSizeMetrics.forEach((operation, sizes) -> {
            if (!sizes.isEmpty()) {
                double avgBatchSize = sizes.stream()
                        .mapToInt(i -> i)
                        .average()
                        .orElse(0);

                List<Long> processingTimes = batchProcessingTimes.get(operation);
                double avgProcessingTime = processingTimes.stream()
                        .mapToLong(l -> l)
                        .average()
                        .orElse(0);

                log.info("{} Batch Operation:", operation);
                log.info("  Average Batch Size: {}", avgBatchSize);
                log.info("  Average Processing Time: {}ms", avgProcessingTime);
                log.info("  Messages per Second: {}",
                        avgBatchSize / (avgProcessingTime / 1000.0));
            }
        });
    }

    private void printMemoryMetrics() {
        log.info("\n--- Memory Usage Metrics ---");

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        log.info("JVM Memory Usage:");
        log.info("  Total Memory: {} MB", totalMemory / (1024 * 1024));
        log.info("  Used Memory: {} MB", usedMemory / (1024 * 1024));
        log.info("  Free Memory: {} MB", freeMemory / (1024 * 1024));
    }
}
