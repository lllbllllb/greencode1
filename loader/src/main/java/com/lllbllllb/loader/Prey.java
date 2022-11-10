package com.lllbllllb.loader;

import java.util.Map;

import lombok.Data;
import org.springframework.http.HttpMethod;

@Data
public class Prey {

    private String name;

    private String path;

    private HttpMethod method;

    private String requestParameters;

    private Map<String, String> headers;

    private String requestBody;

    private long timeoutMs;

    private int expectedResponseStatusCode;

}
