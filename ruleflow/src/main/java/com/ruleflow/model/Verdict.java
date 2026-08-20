package com.ruleflow.model;


public enum Verdict {
    APPROVE(0),
    REVIEW(1),
    REJECT(2);

    private final int severity;

    Verdict(int severity) {
        this.severity = severity;
    }
    public boolean isMoreSevereThan(Verdict other) {
        return this.severity > other.severity;
    }
}
