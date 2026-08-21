package com.ruleflow.core;

import com.ruleflow.model.EngineVerdict;
import com.ruleflow.model.RuleResult;
import com.ruleflow.model.Transaction;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RuleEngine implements AutoCloseable {

    private final List<Rule> rules;
    private final ExecutorService executor;

    public RuleEngine(List<Rule> rules, int threadPoolSize) {
        this.rules = List.copyOf(rules);
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public CompletableFuture<EngineVerdict> evaluateAsync(Transaction transaction) {
        List<CompletableFuture<RuleResult>> futures = rules.stream()
                .map(rule -> CompletableFuture.supplyAsync(() -> rule.evaluate(transaction), executor))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<RuleResult> results = futures.stream().map(CompletableFuture::join).toList();
                    return EngineVerdict.aggregate(transaction, results);
                });
    }

    public EngineVerdict evaluate(Transaction transaction) {
        return evaluateAsync(transaction).join();
    }

    public List<EngineVerdict> evaluateBatch(List<Transaction> transactions) {
        List<CompletableFuture<EngineVerdict>> futures = transactions.stream()
                .map(this::evaluateAsync)
                .toList();
        return futures.stream().map(CompletableFuture::join).toList();
    }

}