package com.lllbllllb.loader;

import lombok.Value;

@Value
public class LoadQuaintResult {

    String serviceName;

    long startMs;

    long endMs;

    Summary summary;

    long totalCount;

    public enum Summary {
        SUCCESS,
        TIMEOUT,
        SERVER_ERROR
    }
}
