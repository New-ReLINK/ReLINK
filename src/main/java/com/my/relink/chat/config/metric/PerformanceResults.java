package com.my.relink.chat.config.metric;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerformanceResults {
    private int numberOfChatRooms;
    private int messagesPerChat;
    private int totalMessages;
    private double totalTimeSeconds;
    private double messagesPerSecond;
    private double transactionsPerSecond;
    private double successRate;
    private double averageLatency;
    private long p95Latency;
    private long p99Latency;
    private long actualDBCount;
}
