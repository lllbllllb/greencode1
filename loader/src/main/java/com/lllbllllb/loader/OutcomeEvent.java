package com.lllbllllb.loader;

import lombok.Value;

@Value
public class OutcomeEvent {

    String serviceName;

    long startMs;

    long endMs;

    boolean success;

}
