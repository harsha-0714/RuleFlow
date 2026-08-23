package com.ruleflow.persistence;

public record JdbcConfig(String url, String user, String password) {

    public static JdbcConfig fromEnv() {
        String url = System.getenv().getOrDefault("RULEFLOW_DB_URL",
                "jdbc:mysql://localhost:3306/ruleflow?useSSL=false&serverTimezone=UTC");
        String user = System.getenv().getOrDefault("RULEFLOW_DB_USER", "root");
        String password = System.getenv().getOrDefault("RULEFLOW_DB_PASSWORD", "");
        return new JdbcConfig(url, user, password);
    }
}