package com.ruleflow.persistence;

import com.ruleflow.model.RuleHit;
import com.ruleflow.model.Verdict;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RuleHitRepository {

    private final ConnectionPool pool;

    public RuleHitRepository(ConnectionPool pool) {
        this.pool = pool;
    }

    private static final String INSERT_SQL =
            "INSERT INTO rule_hits (transaction_id, account_id, rule_name, verdict, reason, created_at) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

    public RuleHit save(RuleHit hit) {
        Connection conn = borrow();
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            bindHit(ps, hit);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                long id = keys.next() ? keys.getLong(1) : -1L;
                return new RuleHit(id, hit.transactionId(), hit.accountId(), hit.ruleName(),
                        hit.verdict(), hit.reason(), hit.createdAt());
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to save rule hit for " + hit.transactionId(), e);
        } finally {
            pool.release(conn);
        }
    }

    public void saveAll(List<RuleHit> hits) {
        if (hits.isEmpty()) return;
        Connection conn = borrow();
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
            for (RuleHit hit : hits) {
                bindHit(ps, hit);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new PersistenceException("Failed to batch-save " + hits.size() + " rule hits", e);
        } finally {
            pool.release(conn);
        }
    }

    public List<RuleHit> findRecentByAccount(String accountId, int limit) {
        String sql = "SELECT id, transaction_id, account_id, rule_name, verdict, reason, created_at " +
                     "FROM rule_hits WHERE account_id = ? ORDER BY created_at DESC LIMIT ?";
        Connection conn = borrow();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<RuleHit> results = new ArrayList<>();
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new PersistenceException("Failed to query hits for account " + accountId, e);
        } finally {
            pool.release(conn);
        }
    }

    private void bindHit(PreparedStatement ps, RuleHit hit) throws SQLException {
        ps.setString(1, hit.transactionId());
        ps.setString(2, hit.accountId());
        ps.setString(3, hit.ruleName());
        ps.setString(4, hit.verdict().name());
        ps.setString(5, hit.reason());
        ps.setTimestamp(6, Timestamp.from(hit.createdAt()));
    }

    private RuleHit mapRow(ResultSet rs) throws SQLException {
        return new RuleHit(
                rs.getLong("id"),
                rs.getString("transaction_id"),
                rs.getString("account_id"),
                rs.getString("rule_name"),
                Verdict.valueOf(rs.getString("verdict")),
                rs.getString("reason"),
                rs.getTimestamp("created_at").toInstant()
        );
    }

    private Connection borrow() {
        try {
            return pool.borrow();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PersistenceException("Interrupted while borrowing a connection", e);
        }
    }
}