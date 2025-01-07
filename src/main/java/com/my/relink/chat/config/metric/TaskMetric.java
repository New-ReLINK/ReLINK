package com.my.relink.chat.config.metric;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TaskMetric {
    private long queuedTime;
    private long startTime;
    private long completionTime;
    private long waitDuration;
    private long processDuration;
}
