package com.ruleflow;

import com.ruleflow.blacklist.BloomFilter;
import com.ruleflow.velocity.SlidingWindow;
import java.time.Duration;
import java.time.Instant;

public class App {
    public static void main(String[] args) {
        SlidingWindow w = new SlidingWindow(Duration.ofSeconds(60));
        System.out.println("count: " + w.recordAndCount(Instant.now()));

        BloomFilter f = new BloomFilter(1000, 0.01);
        f.add("acct-1");
        System.out.println("might contain acct-1: " + f.mightContain("acct-1"));
        System.out.println("might contain acct-2: " + f.mightContain("acct-2"));
    }
}