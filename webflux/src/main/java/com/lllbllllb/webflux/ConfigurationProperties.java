package com.lllbllllb.webflux;

import java.time.Duration;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@org.springframework.boot.context.properties.ConfigurationProperties("webflux-service")
public class ConfigurationProperties {

    private String slowpokeHost;

    private WebClientConfig webClientConfig = new WebClientConfig();

    @Data
    public static class WebClientConfig {

        private int pendingAcquireMaxCount;

        private int maxConnections;

        private Duration responseTimeout;

    }

}
