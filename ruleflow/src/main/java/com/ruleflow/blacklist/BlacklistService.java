package com.ruleflow.blacklist;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class BlacklistService {

    private final BloomFilter bloomFilter;
    private final Set<String> confirmed = ConcurrentHashMap.newKeySet();

    public BlacklistService(int expectedEntries, double falsePositiveRate) {
        this.bloomFilter = new BloomFilter(expectedEntries, falsePositiveRate);
    }
    public void blacklist(String key) {
        bloomFilter.add(key);
        confirmed.add(key);
    }
    public boolean isBlacklisted(String key) {
        if (!bloomFilter.mightContain(key)) {
            return false;
        }
        return confirmed.contains(key);
    }
}
