package com.ruleflow;

import com.ruleflow.blacklist.BlacklistService;
import com.ruleflow.core.RuleChain;
import com.ruleflow.model.EngineVerdict;
import com.ruleflow.model.RuleHit;
import com.ruleflow.model.Transaction;
import com.ruleflow.persistence.ConnectionPool;
import com.ruleflow.persistence.JdbcConfig;
import com.ruleflow.persistence.RuleHitRepository;
import com.ruleflow.rules.BlacklistRule;
import com.ruleflow.rules.VelocityRule;
import com.ruleflow.velocity.VelocityTracker;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class PersistenceDemo {

    public static void main(String[] args) {
        JdbcConfig config = JdbcConfig.fromEnv();

        try (ConnectionPool pool = new ConnectionPool(config, 4)) {
            RuleHitRepository repository = new RuleHitRepository(pool);

            VelocityTracker velocityTracker = new VelocityTracker(Duration.ofSeconds(60));
            BlacklistService blacklistService = new BlacklistService(1000, 0.01);
            blacklistService.blacklist("acct-blocked");

            RuleChain chain = new RuleChain(false)
                    .addRule(new BlacklistRule(blacklistService))
                    .addRule(new VelocityRule(velocityTracker, 3));

            Instant now = Instant.now();
            Transaction t1 = tx("tx-p1", "acct-1", "50.00", now);
            Transaction t2 = tx("tx-p2", "acct-blocked", "999.00", now);

            for (Transaction t : List.of(t1, t2)) {
                EngineVerdict verdict = chain.evaluate(t);
                List<RuleHit> hits = verdict.ruleResults().stream()
                        .map(result -> RuleHit.of(t, result))
                        .toList();
                repository.saveAll(hits);
                System.out.println("saved " + hits.size() + " rule hits for " + t.transactionId());
            }

            System.out.println("\nrecent hits for acct-blocked:");
            repository.findRecentByAccount("acct-blocked", 10)
                    .forEach(h -> System.out.println("  " + h.ruleName() + ": " + h.verdict() + " - " + h.reason()));
        }
    }

    private static Transaction tx(String id, String account, String amount, Instant time) {
        return new Transaction(id, account, new BigDecimal(amount), "merchant-1", "IN", time);
    }
}