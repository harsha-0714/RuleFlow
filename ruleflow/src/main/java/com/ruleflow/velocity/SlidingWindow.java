package com.ruleflow.velocity;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Tracks event timestamps within a trailing time window and answers
 * "how many events occurred in the last N seconds?" in O(1) amortized time.
 *
 * WHY A DEQUE, NOT A LIST OR A SIMPLE COUNTER (interview answer):
 *   A naive approach is "counter++ every event, counter-- every window-length
 *   seconds" — but that's a FIXED window, not sliding, and suffers the classic
 *   boundary bug: 100 events at 0:59 and 100 more at 1:01 both land in
 *   different fixed buckets and look like 100/window each, even though any
 *   sliding 60s view sees 200. A true sliding window needs to know the exact
 *   timestamp of each event so it can evict only the ones that have aged out.
 *
 *   We use an ArrayDeque as the timestamp store because we only ever need:
 *     - push newest to the tail  -> addLast(), O(1)
 *     - evict oldest from the head while it's outside the window -> peekFirst()
 *       / pollFirst(), O(1)
 *   Both are queue operations, and ArrayDeque gives O(1) amortized for both
 *   ends without the node-pointer overhead of a LinkedList. This is the
 *   textbook "sliding window" data structure — same idea behind the
 *   "maximum sum subarray of size k" family of problems, generalized to a
 *   time-based (not count-based) window.
 *
 * WHY SYNCHRONIZED METHODS (thread-safety):
 *   Many transactions for the SAME account can arrive concurrently from
 *   different worker threads (see RuleEngine's ExecutorService in Step 4).
 *   ArrayDeque is NOT thread-safe by itself — concurrent addLast/pollFirst
 *   calls can corrupt its internal array. We guard the whole read-evict-count
 *   sequence with `synchronized` because it must be atomic: if thread A is
 *   mid-eviction while thread B reads size(), B could see a stale count.
 *   A ConcurrentLinkedDeque would make individual ops thread-safe but NOT
 *   the compound "evict-then-count" operation atomic — that's a common
 *   interview trap ("does using a concurrent collection mean my code is
 *   thread-safe?" -> not automatically, not for compound actions).
 */
public class SlidingWindow {

    private final Deque<Instant> timestamps = new ArrayDeque<>();
    private final Duration windowSize;

    public SlidingWindow(Duration windowSize) {
        this.windowSize = windowSize;
    }

    /** Record a new event "now" and return the count within the window (inclusive of this event). */
    public synchronized int recordAndCount(Instant now) {
        timestamps.addLast(now);
        evictExpired(now);
        return timestamps.size();
    }

    /** Count events currently in the window without recording a new one. */
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
