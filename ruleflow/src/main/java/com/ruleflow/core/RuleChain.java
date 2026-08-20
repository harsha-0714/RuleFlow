package com.ruleflow.core;

import com.ruleflow.model.EngineVerdict;
import com.ruleflow.model.RuleResult;
import com.ruleflow.model.Transaction;
import com.ruleflow.model.Verdict;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class RuleChain {

    private final List<Rule> rules = new ArrayList<>();
    private final boolean failFast;

    public RuleChain(boolean failFast) {
        this.failFast = failFast;
    }

    public RuleChain addRule(Rule rule) {
        rules.add(rule);
        rules.sort(Comparator.comparingInt(Rule::priority));
        return this;
    }

    public List<Rule> rules() {
        return List.copyOf(rules);
    }
    public EngineVerdict evaluate(Transaction transaction) {
        List<RuleResult> results = new ArrayList<>();
        for (Rule rule : rules) {
            RuleResult result = rule.evaluate(transaction);
            results.add(result);
            if (failFast && result.verdict() == Verdict.REJECT) {
                break;
            }
        }
        return EngineVerdict.aggregate(transaction, results);
    }
}
