package com.ruleflow;

import com.ruleflow.blacklist.BlacklistService;
import com.ruleflow.core.RuleChain;
import com.ruleflow.model.EngineVerdict;
import com.ruleflow.model.Transaction;
import com.ruleflow.rules.BlacklistRule;
import com.ruleflow.rules.VelocityRule;
import com.ruleflow.velocity.VelocityTracker;

import com.ruleflow.core.Rule;
import com.ruleflow.core.RuleEngine;
import java.util.List;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public class App {

    public static void main(String[] args) {
        VelocityTracker velocityTracker = new VelocityTracker(Duration.ofSeconds(60));
        BlacklistService blacklistService = new BlacklistService(1000, 0.01);
        blacklistService.blacklist("acct-blocked");

        RuleChain chain = new RuleChain(true)
                .addRule(new BlacklistRule(blacklistService))
                .addRule(new VelocityRule(velocityTracker, 3));

        Instant now = Instant.now();
        evaluate(chain, tx("tx-1", "acct-1", "50.00", now));
        evaluate(chain, tx("tx-2", "acct-1", "20.00", now.plusSeconds(5)));
        evaluate(chain, tx("tx-3", "acct-1", "20.00", now.plusSeconds(10)));
        evaluate(chain, tx("tx-4", "acct-1", "20.00", now.plusSeconds(15)));
        evaluate(chain, tx("tx-5", "acct-blocked", "999.00", now));
    
        System.out.println("\n--- multithreaded engine, batch of transactions ---");
        List<Rule> ruleList = List.of(new BlacklistRule(blacklistService), new VelocityRule(velocityTracker, 3));
        try (RuleEngine engine = new RuleEngine(ruleList, 4)) {
            List<Transaction> batch = List.of(
                    tx("tx-6", "acct-2", "10.00", now),
                    tx("tx-7", "acct-2", "10.00", now.plusSeconds(1)),
                    tx("tx-8", "acct-3", "500.00", now),
                    tx("tx-9", "acct-blocked", "1.00", now)
            );
            List<EngineVerdict> verdicts = engine.evaluateBatch(batch);
            verdicts.forEach(v -> {
                System.out.println(v.transaction().transactionId() + " -> " + v.finalVerdict());
                v.ruleResults().forEach(r ->
                        System.out.println("   " + r.ruleName() + ": " + r.verdict() + " (" + r.reason() + ")"));
            });
        }
    }

    private static Transaction tx(String id, String account, String amount, Instant time) {
        return new Transaction(id, account, new BigDecimal(amount), "merchant-1", "IN", time);
    }

    private static void evaluate(RuleChain chain, Transaction transaction) {
        EngineVerdict verdict = chain.evaluate(transaction);
        System.out.println(transaction.transactionId() + " -> " + verdict.finalVerdict());
        verdict.ruleResults().forEach(r ->
                System.out.println("   " + r.ruleName() + ": " + r.verdict() + " (" + r.reason() + ")"));
    }
}