package com.ruleflow.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConnectionPool implements AutoCloseable {

    private final BlockingQueue<Connection> pool;

    public ConnectionPool(JdbcConfig config, int poolSize) {
        this.pool = new ArrayBlockingQueue<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            try {
                Connection conn = DriverManager.getConnection(config.url(), config.user(), config.password());
                pool.put(conn);
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to initialize connection pool", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while initializing pool", e);
            }
        }
    }

    public Connection borrow() throws InterruptedException {
        return pool.take();
    }

    public Connection borrow(long timeoutMs) throws InterruptedException {
        return pool.poll(timeoutMs, TimeUnit.MILLISECONDS);
    }

    public void release(Connection connection) {
        pool.offer(connection);
    }

    @Override
    public void close() {
        pool.forEach(conn -> {
            try {
                conn.close();
            } catch (SQLException ignored) {
            }
        });
    }
}