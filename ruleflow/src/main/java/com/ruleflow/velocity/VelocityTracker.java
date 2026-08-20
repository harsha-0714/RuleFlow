package com.ruleflow.velocity;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
public class VelocityTracker {

    private final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final Duration windowSize;

    public VelocityTracker(Duration windowSize) {
        this.windowSize = windowSize;
    }
    public int recordAndCount(String key, Instant now) {
        return windows.computeIfAbsent(key, k -> new SlidingWindow(windowSize))
                       .recordAndCount(now);
    }
    public int trackedKeyCount() {
        return windows.size();
    }
}
