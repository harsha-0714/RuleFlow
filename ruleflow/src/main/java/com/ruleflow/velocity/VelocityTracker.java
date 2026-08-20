package com.ruleflow.velocity;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains one SlidingWindow per key (e.g. per accountId) so we can answer
 * "how many transactions has THIS account made in the last N seconds?"
 * independently for every account, without accounts blocking each other.
 *
 * WHY ConcurrentHashMap + computeIfAbsent (not a plain HashMap + synchronized block):
 *   - A plain HashMap is not thread-safe at all: concurrent put() calls during
 *     resize can corrupt it or infinite-loop (a classic real-world outage cause
 *     pre-Java 8). ConcurrentHashMap uses lock striping internally so different
 *     keys can be read/written concurrently without one global lock.
 *   - computeIfAbsent is atomic: without it, two threads could both check
 *     "does this key exist?", both see "no", and both create a NEW SlidingWindow
 *     for the same account — silently losing one thread's counts (a classic
 *     check-then-act race condition). computeIfAbsent guarantees only one
 *     SlidingWindow is ever created per key, even under concurrent access.
 *
 *   This is a very common interview question: "how would you build a
 *   thread-safe cache/counter keyed by user ID?" — this class is that answer.
 */
public class VelocityTracker {

    private final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();
    private final Duration windowSize;

    public VelocityTracker(Duration windowSize) {
        this.windowSize = windowSize;
    }

    /** Record an event for `key` at `now`, return the count within the window. */
    public int recordAndCount(String key, Instant now) {
        return windows.computeIfAbsent(key, k -> new SlidingWindow(windowSize))
                       .recordAndCount(now);
    }

    /** Number of distinct keys currently being tracked (useful for metrics/tests). */
    public int trackedKeyCount() {
        return windows.size();
    }
}
