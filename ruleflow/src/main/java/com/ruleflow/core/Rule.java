package com.ruleflow.core;

import com.ruleflow.model.RuleResult;
import com.ruleflow.model.Transaction;

public interface Rule {
    RuleResult evaluate(Transaction transaction);
    String name();

    default int priority() {
        return 100;
    }
}
