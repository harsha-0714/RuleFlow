CREATE DATABASE IF NOT EXISTS ruleflow;
USE ruleflow;

CREATE TABLE IF NOT EXISTS rule_hits (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64)  NOT NULL,
    account_id     VARCHAR(64)  NOT NULL,
    rule_name      VARCHAR(128) NOT NULL,
    verdict        VARCHAR(16)  NOT NULL,
    reason         VARCHAR(512) NOT NULL,
    created_at     TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),

    INDEX idx_transaction_id (transaction_id),
    INDEX idx_account_created (account_id, created_at DESC),
    INDEX idx_verdict_created (verdict, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;