package com.lllbllllb.loader;

import java.time.Duration;
import java.util.Map;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@org.springframework.boot.context.properties.ConfigurationProperties("loader-service")
public class ConfigurationProperties {

    private Map<String, Service> services;

    private WebClientConfig webClientConfig = new WebClientConfig();

    @Data
    public static class Service {

        private String host;

    }

    @Data
    public static class WebClientConfig {

        private int pendingAcquireMaxCount = 100_000;

        private int maxConnections = 42;

        private Duration responseTimeout = Duration.ofSeconds(20);

    }
}
