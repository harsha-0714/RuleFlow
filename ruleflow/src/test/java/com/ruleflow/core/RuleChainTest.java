package com.ruleflow.core;

import com.ruleflow.model.EngineVerdict;
import com.ruleflow.model.RuleResult;
import com.ruleflow.model.Transaction;
import com.ruleflow.model.Verdict;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class RuleChainTest {

    private Transaction sampleTransaction() {
        return new Transaction("tx-1", "acct-1", new BigDecimal("100.00"), "merchant-1", "US", Instant.now());
    }

    /** Simple test double implementing the Strategy interface. */
    private Rule fixedRule(String name, Verdict verdict, int priority) {
        return new Rule() {
            public RuleResult evaluate(Transaction t) {
                return new RuleResult(name, verdict, "test");
            }
            public String name() { return name; }
            public int priority() { return priority; }
        };
    }

    @Test
    void aggregatesToMostSevereVerdict() {
        RuleChain chain = new RuleChain(false)
                .addRule(fixedRule("approveRule", Verdict.APPROVE, 1))
                .addRule(fixedRule("reviewRule", Verdict.REVIEW, 2));

        EngineVerdict verdict = chain.evaluate(sampleTransaction());

        assertEquals(Verdict.REVIEW, verdict.finalVerdict());
        assertEquals(2, verdict.ruleResults().size());
    }

    @Test
    void failFastStopsAfterFirstReject() {
        RuleChain chain = new RuleChain(true)
                .addRule(fixedRule("rejectRule", Verdict.REJECT, 1))
                .addRule(fixedRule("neverRunsRule", Verdict.APPROVE, 2));

        EngineVerdict verdict = chain.evaluate(sampleTransaction());

        assertEquals(Verdict.REJECT, verdict.finalVerdict());
        assertEquals(1, verdict.ruleResults().size(), "second rule should not have run");
    }

    @Test
    void rulesRunInPriorityOrder() {
        RuleChain chain = new RuleChain(false)
                .addRule(fixedRule("low", Verdict.APPROVE, 50))
                .addRule(fixedRule("high", Verdict.APPROVE, 1));

        assertEquals("high", chain.rules().get(0).name());
        assertEquals("low", chain.rules().get(1).name());
    }
}
