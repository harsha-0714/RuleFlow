package com.ruleflow.blacklist;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Blacklist lookup service. Bloom filter gives a fast "definitely not
 * blacklisted" answer; a confirmed-set backs it up to avoid rejecting on a
 * false positive (in a real system this second check would hit MySQL/Redis).
 */
public class BlacklistService {

    private final BloomFilter bloomFilter;
    private final Set<String> confirmed = ConcurrentHashMap.newKeySet();

    public BlacklistService(int expectedEntries, double falsePositiveRate) {
        this.bloomFilter = new BloomFilter(expectedEntries, falsePositiveRate);
    }

    /** Adds an entity (accountId, merchantId, etc.) to the blacklist. */
    public void blacklist(String key) {
        bloomFilter.add(key);
        confirmed.add(key);
    }

    /**
     * Checks blacklist status. Bloom filter first (O(k), no lookup cost) —
     * if it says "no", we trust that immediately (no false negatives possible).
     * If it says "maybe", we confirm against the real set before flagging.
     */
    public boolean isBlacklisted(String key) {
        if (!bloomFilter.mightContain(key)) {
            return false;
        }
        return confirmed.contains(key);
    }
}
