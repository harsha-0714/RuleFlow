package com.ruleflow.rules;

import com.ruleflow.blacklist.BlacklistService;
import com.ruleflow.core.Rule;
import com.ruleflow.model.RuleResult;
import com.ruleflow.model.Transaction;

public class BlacklistRule implements Rule {

    private final BlacklistService blacklistService;

    public BlacklistRule(BlacklistService blacklistService) {
        this.blacklistService = blacklistService;
    }

    @Override
    public RuleResult evaluate(Transaction transaction) {
        if (blacklistService.isBlacklisted(transaction.accountId())) {
            return RuleResult.reject(name(), "account is blacklisted: " + transaction.accountId());
        }
        if (blacklistService.isBlacklisted(transaction.merchantId())) {
            return RuleResult.reject(name(), "merchant is blacklisted: " + transaction.merchantId());
        }
        return RuleResult.approve(name());
    }

    @Override
    public String name() {
        return "BlacklistRule";
    }

    @Override
    public int priority() {
        return 5;
    }
}
