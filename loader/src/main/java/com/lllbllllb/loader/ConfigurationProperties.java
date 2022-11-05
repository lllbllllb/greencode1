package com.lllbllllb.loader;

import java.time.Duration;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@org.springframework.boot.context.properties.ConfigurationProperties("loader-service")
public class ConfigurationProperties {

    private WebClientConfig webClientConfig = new WebClientConfig();

    private LoaderConfig loaderConfig = new LoaderConfig();

    @Data
    public static class WebClientConfig {

        private int pendingAcquireMaxCount = 100_000;

        private int maxConnections = 42;

        private Duration responseTimeout = Duration.ofSeconds(20);

    }

    @Data
    public static class LoaderConfig {

        private int threadCap = 24;

        private int queuedTaskCap = 0x7fffffff;

    }

}
