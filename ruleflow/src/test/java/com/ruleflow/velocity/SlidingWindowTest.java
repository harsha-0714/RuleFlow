package com.ruleflow.velocity;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SlidingWindowTest {

    @Test
    void countsEventsWithinWindow() {
        SlidingWindow window = new SlidingWindow(Duration.ofSeconds(60));
        Instant t0 = Instant.parse("2026-01-01T00:00:00Z");

        assertEquals(1, window.recordAndCount(t0));
        assertEquals(2, window.recordAndCount(t0.plusSeconds(10)));
        assertEquals(3, window.recordAndCount(t0.plusSeconds(30)));
    }

    @Test
    void evictsEventsOutsideWindow() {
        SlidingWindow window = new SlidingWindow(Duration.ofSeconds(60));
        Instant t0 = Instant.parse("2026-01-01T00:00:00Z");

        window.recordAndCount(t0);
        window.recordAndCount(t0.plusSeconds(10));

        // This event is 61s after t0, so the first event (age 61s) should be evicted,
        // but the second (age 51s) should remain, plus this new one = 2.
        int count = window.recordAndCount(t0.plusSeconds(61));
        assertEquals(2, count);
    }

    @Test
    void handlesBoundaryCrossingBurstCorrectly() {
        // This is exactly the "fixed window boundary bug" scenario described
        // in SlidingWindow's javadoc: a burst straddling a minute boundary
        // must still be seen as one burst by a true sliding window.
        SlidingWindow window = new SlidingWindow(Duration.ofSeconds(60));
        Instant burstStart = Instant.parse("2026-01-01T00:00:59Z");

        for (int i = 0; i < 5; i++) {
            window.recordAndCount(burstStart.minusSeconds(i));
        }
        // 5 events just before the minute mark, then 1 more just after
        int count = window.recordAndCount(burstStart.plusSeconds(2)); // 00:01:01
        assertEquals(6, count, "sliding window must see the full burst regardless of boundary");
    }
}
