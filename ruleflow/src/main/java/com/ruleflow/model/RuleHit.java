package com.ruleflow.model;

import java.time.Instant;

public record RuleHit(
        Long id,
        String transactionId,
        String accountId,
        String ruleName,
        Verdict verdict,
        String reason,
        Instant createdAt
) {
    public static RuleHit of(Transaction transaction, RuleResult result) {
        return new RuleHit(null, transaction.transactionId(), transaction.accountId(),
                result.ruleName(), result.verdict(), result.reason(), Instant.now());
    }
}