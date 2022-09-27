package com.lllbllllb.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@SpringBootApplication
public class WebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }

    @Bean
    static WebClient slowpokeClient(ConfigurationProperties properties) {
        var webClientConfig = properties.getWebClientConfig();
        // https://stackoverflow.com/a/68658096
        var connectionProvider = ConnectionProvider.builder("customConnectionPool")
            .maxConnections(webClientConfig.getMaxConnections())
            .pendingAcquireMaxCount(webClientConfig.getPendingAcquireMaxCount())
            .build();
        var client = HttpClient.create(connectionProvider)
            .responseTimeout(webClientConfig.getResponseTimeout());
        var clientHttpConnector = new ReactorClientHttpConnector(client);

        return WebClient.builder()
            .baseUrl(properties.getSlowpokeHost())
            .clientConnector(clientHttpConnector)
            .build();
    }

}
