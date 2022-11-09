package com.lllbllllb.loader;

import java.util.Map;

import lombok.Data;

@Data
public class Prey {

    private String name;

    private String path;

    private LoadMethod method;

    private String requestParameters;

    private Map<String, String> headers;

    private String requestBody;

    private long timeoutMs;

}
