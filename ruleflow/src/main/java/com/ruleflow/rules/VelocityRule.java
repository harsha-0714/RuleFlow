package com.ruleflow.rules;

import com.ruleflow.core.Rule;
import com.ruleflow.model.RuleResult;
import com.ruleflow.model.Transaction;
import com.ruleflow.velocity.VelocityTracker;

public class VelocityRule implements Rule {

    private final VelocityTracker tracker;
    private final int maxCount;

    public VelocityRule(VelocityTracker tracker, int maxCount) {
        this.tracker = tracker;
        this.maxCount = maxCount;
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        int count = tracker.recordAndCount(transaction.accountId(), transaction.timestamp());
        if (count > maxCount) {
            return RuleResult.reject(name(),
                    "velocity exceeded: " + count + " transactions in window (max " + maxCount + ")");
        }
        return RuleResult.approve(name());
    }

    @Override
    public String name() {
        return "VelocityRule";
    }

    @Override
    public int priority() {
        return 10; // cheap in-memory check, run early
    }
}
