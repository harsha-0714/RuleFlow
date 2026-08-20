package com.ruleflow.model;

import java.time.Instant;
import java.math.BigDecimal;


public record Transaction(
        String transactionId,
        String accountId,
        BigDecimal amount,
        String merchantId,
        String country,
        Instant timestamp
) {
    public Transaction {
        if (transactionId == null || transactionId.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
        if (accountId == null || accountId.isBlank()) {
            throw new IllegalArgumentException("accountId must not be blank");
        }
        if (amount == null || amount.signum() < 0) {
            throw new IllegalArgumentException("amount must be non-negative");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("timestamp must not be null");
        }
    }
}
