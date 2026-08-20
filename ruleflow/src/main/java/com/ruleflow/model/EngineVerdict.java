package com.ruleflow.model;

import java.util.List;

public record EngineVerdict(Transaction transaction, Verdict finalVerdict, List<RuleResult> ruleResults) {

    public static EngineVerdict aggregate(Transaction transaction, List<RuleResult> results) {
        Verdict worst = Verdict.APPROVE;
        for (RuleResult r : results) {
            if (r.verdict().isMoreSevereThan(worst)) {
                worst = r.verdict();
            }
        }
        return new EngineVerdict(transaction, worst, List.copyOf(results));
    }
}
