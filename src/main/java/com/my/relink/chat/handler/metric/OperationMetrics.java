package com.my.relink.chat.handler.metric;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicLong;

public  class OperationMetrics {
    private final AtomicLong count = new AtomicLong(0);
    private final AtomicLong totalLatency = new AtomicLong(0);
    private final AtomicLong maxLatency = new AtomicLong(0);
    private final ConcurrentSkipListSet<Long> latencies = new ConcurrentSkipListSet<>();

    public OperationMetrics addLatency(long latency) {
        count.incrementAndGet();
        totalLatency.addAndGet(latency);
        updateMaxLatency(latency);
        latencies.add(latency);
        return this;
    }

    private void updateMaxLatency(long latency) {
        long currentMax;
        do {
            currentMax = maxLatency.get();
            if (latency <= currentMax) break;
        } while (!maxLatency.compareAndSet(currentMax, latency));
    }

    public long getCount() {
        return count.get();
    }

    public double getAverageLatency() {
        long currentCount = count.get();
        return currentCount > 0 ?
                (double) totalLatency.get() / currentCount : 0;
    }

    public long getMaxLatency() {
        return maxLatency.get();
    }

    public long get95thPercentile() {
        List<Long> sortedLatencies = new ArrayList<>(latencies);
        if (sortedLatencies.isEmpty()) return 0;
        int index = (int) Math.ceil(sortedLatencies.size() * 0.95) - 1;
        return sortedLatencies.get(Math.max(0, Math.min(index, sortedLatencies.size() - 1)));
    }

    @Getter
    public static class MetricsDTO {
        long count;
        double averageLatency;
        long maxLatency;
        long percentile95;

        public MetricsDTO(long count, double averageLatency, long maxLatency, long percentile95) {
            this.count = count;
            this.averageLatency = averageLatency;
            this.maxLatency = maxLatency;
            this.percentile95 = percentile95;
        }
    }
}
