package com.ruleflow.model;

public record RuleResult(String ruleName, Verdict verdict, String reason) {

    public static RuleResult approve(String ruleName) {
        return new RuleResult(ruleName, Verdict.APPROVE, "no violation");
    }

    public static RuleResult reject(String ruleName, String reason) {
        return new RuleResult(ruleName, Verdict.REJECT, reason);
    }

    public static RuleResult review(String ruleName, String reason) {
        return new RuleResult(ruleName, Verdict.REVIEW, reason);
    }
}
