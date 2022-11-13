package com.lllbllllb.loader;

public record AttemptResult(
    long responseTime,

    Status status,
    long attemptNumber,

    long successCount,

    long timeoutCount,

    long errorCount

) {

    public enum Status {
        SUCCESS,
        TIMEOUT,
        UNEXPECTED_STATUS
    }
}
