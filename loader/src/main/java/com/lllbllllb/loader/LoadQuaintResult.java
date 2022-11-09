package com.lllbllllb.loader;

public record LoadQuaintResult(
    String serviceName,
    long elapsed,
    Summary summary,
    long totalCount
) {

    public enum Summary {
        SUCCESS,
        TIMEOUT,
        SERVER_ERROR
    }
}
