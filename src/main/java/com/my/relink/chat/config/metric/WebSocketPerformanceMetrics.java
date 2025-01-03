package com.my.relink.chat.config.metric;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class WebSocketPerformanceMetrics {
    private final AtomicLong completedTransactions = new AtomicLong(0);
    private final AtomicLong failedTransactions = new AtomicLong(0);
    private final List<Long> transactionLatencies = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<Long>> operationLatencies = new ConcurrentHashMap<>();
    private long startTime;
    private long endTime;

    public void startMeasurement() {
        this.startTime = System.currentTimeMillis();
    }

    public void endMeasurement() {
        this.endTime = System.currentTimeMillis();
    }

    public void recordTransaction(boolean success, long latency) {
        if (success) {
            completedTransactions.incrementAndGet();
            transactionLatencies.add(latency);
        } else {
            failedTransactions.incrementAndGet();
        }
    }

    public void recordOperation(String operation, long latency) {
        operationLatencies.computeIfAbsent(operation, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(latency);
    }

    public PerformanceResults calculateResults(int numberOfChatRooms, int messagesPerChat, long actualDBCount) {
        double totalTimeSeconds = (endTime - startTime) / 1000.0;
        int totalMessages = numberOfChatRooms * messagesPerChat;
        double messagesPerSecond = totalMessages / totalTimeSeconds;

        // 트랜잭션 관련 계산
        long totalTransactions = completedTransactions.get() + failedTransactions.get();
        double transactionsPerSecond = completedTransactions.get() / totalTimeSeconds;
        double successRate = (completedTransactions.get() * 100.0) / totalTransactions;

        // 레이턴시 계산
        List<Long> sortedLatencies = transactionLatencies.stream()
                .sorted()
                .collect(Collectors.toList());

        PerformanceResults results = new PerformanceResults();
        if (!sortedLatencies.isEmpty()) {
            results.setAverageLatency(sortedLatencies.stream().mapToLong(l -> l).average().orElse(0));
            results.setP95Latency(sortedLatencies.get((int)(sortedLatencies.size() * 0.95)));
            results.setP99Latency(sortedLatencies.get((int)(sortedLatencies.size() * 0.99)));
        }

        results.setNumberOfChatRooms(numberOfChatRooms);
        results.setMessagesPerChat(messagesPerChat);
        results.setTotalMessages(totalMessages);
        results.setTotalTimeSeconds(totalTimeSeconds);
        results.setMessagesPerSecond(messagesPerSecond);
        results.setTransactionsPerSecond(transactionsPerSecond);
        results.setSuccessRate(successRate);
        results.setActualDBCount(actualDBCount);

        return results;
    }

    public void printResults(int numberOfChatRooms, int messagesPerChat, long actualDBCount) {
        PerformanceResults results = calculateResults(numberOfChatRooms, messagesPerChat, actualDBCount);

        log.info("\n=== WebSocket Chat Performance Test Results ===");
        log.info("Number of Chat Rooms: {}", results.getNumberOfChatRooms());
        log.info("Messages per Chat Room: {}", results.getMessagesPerChat());
        log.info("Total Messages: {}", results.getTotalMessages());
        log.info("Total Time: {} seconds", results.getTotalTimeSeconds());
        log.info("Messages per Second (RPS): {}", String.format("%.2f", results.getMessagesPerSecond()));
        log.info("Transactions per Second (TPS): {}", String.format("%.2f", results.getTransactionsPerSecond()));
        log.info("Success Rate: {}%", String.format("%.2f", results.getSuccessRate()));
        log.info("Average Latency: {} ms", String.format("%.2f", results.getAverageLatency()));
        log.info("95th Percentile Latency: {} ms", results.getP95Latency());
        log.info("99th Percentile Latency: {} ms", results.getP99Latency());
        log.info("Successfully Processed Messages: {}", results.getActualDBCount());

        // 각 작업별 성능 지표
        log.info("\n=== Operation-wise Performance ===");
        operationLatencies.forEach((operation, latencies) -> {
            if (!latencies.isEmpty()) {
                List<Long> sorted = new ArrayList<>(latencies);
                Collections.sort(sorted);
                double avg = sorted.stream().mapToLong(l -> l).average().orElse(0);
                long p95 = sorted.get((int)(sorted.size() * 0.95));

                log.info("{} Operation:", operation);
                log.info("  Count: {}", latencies.size());
                log.info("  Average: {}ms", String.format("%.2f", avg));
                log.info("  95th percentile: {}ms", p95);
            }
        });
    }
}
