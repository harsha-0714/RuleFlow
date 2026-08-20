package com.ruleflow.blacklist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BloomFilterTest {

    @Test
    void neverFalseNegative() {
        BloomFilter filter = new BloomFilter(1000, 0.01);
        for (int i = 0; i < 1000; i++) {
            filter.add("account-" + i);
        }
        for (int i = 0; i < 1000; i++) {
            assertTrue(filter.mightContain("account-" + i), "must never false-negative on inserted value");
        }
    }

    @Test
    void falsePositiveRateIsRoughlyBounded() {
        BloomFilter filter = new BloomFilter(1000, 0.01);
        for (int i = 0; i < 1000; i++) {
            filter.add("account-" + i);
        }
        int falsePositives = 0;
        int trials = 10000;
        for (int i = 0; i < trials; i++) {
            if (filter.mightContain("unrelated-" + i)) {
                falsePositives++;
            }
        }
        double observedRate = (double) falsePositives / trials;
        assertTrue(observedRate < 0.05, "observed FP rate " + observedRate + " should stay close to target 0.01");
    }
}
