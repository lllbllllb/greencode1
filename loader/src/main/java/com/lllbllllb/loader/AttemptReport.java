package com.lllbllllb.loader;

public record AttemptReport(
    String serviceName,

    long responseTime,

    AttemptResult attemptResult,
    long attemptNumber,

    long successCount,

    long timeoutCount,

    long errorCount

) {

    public enum AttemptResult {
        SUCCESS,
        TIMEOUT,
        UNEXPECTED_STATUS
    }
}
