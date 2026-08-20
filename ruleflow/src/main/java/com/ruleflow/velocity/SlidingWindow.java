package com.ruleflow.velocity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
public class SlidingWindow {

    private final Deque<Instant> timestamps = new ArrayDeque<>();
    private final Duration windowSize;

    public SlidingWindow(Duration windowSize) {
        this.windowSize = windowSize;
    }
    public synchronized int recordAndCount(Instant now) {
        timestamps.addLast(now);
        evictExpired(now);
        return timestamps.size();
    }
    public synchronized int count(Instant now) {
        evictExpired(now);
        return timestamps.size();
    }

    private void evictExpired(Instant now) {
        Instant cutoff = now.minus(windowSize);
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(cutoff)) {
            timestamps.pollFirst();
        }
    }
}
