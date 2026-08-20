package com.ruleflow.blacklist;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;
import java.util.concurrent.ThreadLocalRandom;

public class BloomFilter {

    private final BitSet bits;
    private final int size;          // m: number of bits
    private final int hashCount;     // k: number of hash functions
    private final long seed1;
    private final long seed2;

    public BloomFilter(int expectedInsertions, double falsePositiveRate) {
        this.size = optimalBitSize(expectedInsertions, falsePositiveRate);
        this.hashCount = optimalHashCount(size, expectedInsertions);
        this.bits = new BitSet(size);
        this.seed1 = ThreadLocalRandom.current().nextLong();
        this.seed2 = ThreadLocalRandom.current().nextLong();
    }

    /** m = -(n * ln(p)) / (ln(2)^2) — optimal bit array size for target false-positive rate p. */
    private static int optimalBitSize(int n, double p) {
        return (int) Math.ceil(-(n * Math.log(p)) / (Math.log(2) * Math.log(2)));
    }

    /** k = (m/n) * ln(2) — optimal number of hash functions. */
    private static int optimalHashCount(int m, int n) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    /** Adds a value by setting k bits derived from double hashing. */
    public void add(String value) {
        for (int i = 0; i < hashCount; i++) {
            bits.set(indexFor(value, i));
        }
    }

    /** Returns true if value MIGHT be present (false positives possible, never false negatives). */
    public boolean mightContain(String value) {
        for (int i = 0; i < hashCount; i++) {
            if (!bits.get(indexFor(value, i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Double hashing trick: simulate k independent hash functions from just two
     * base hashes (h1, h2) via h_i(x) = h1(x) + i*h2(x), avoiding k separate
     * hash implementations while keeping bit positions well distributed.
     */
    private int indexFor(String value, int i) {
        long h1 = hash(value, seed1);
        long h2 = hash(value, seed2);
        long combined = h1 + (long) i * h2;
        int idx = (int) (combined % size);
        return Math.abs(idx);
    }

    private long hash(String value, long seed) {
        long h = seed;
        for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
            h = 31 * h + b;
        }
        return h;
    }

    public int bitSize() {
        return size;
    }

    public int hashCount() {
        return hashCount;
    }
}
