package com.lllbllllb.webflux;

import java.time.Duration;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@org.springframework.boot.context.properties.ConfigurationProperties("web-service")
public class ConfigurationProperties {

    private String slowpokeHost = "http://localhost:8080";

    private WebClientConfig webClientConfig;

    @Data
    public static class WebClientConfig {

        private int pendingAcquireMaxCount;

        private int maxConnections;

        private Duration responseTimeout;

    }

}
