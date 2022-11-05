package com.lllbllllb.loader;

import lombok.RequiredArgsConstructor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Service
@RequiredArgsConstructor
public class WebClientFactory {

    private final ConfigurationProperties properties;

    public WebClient create(String path, String connectionPoolName) {
        var webClientConfig = properties.getWebClientConfig();
        var connectionProvider = ConnectionProvider.builder("loaderConnectionPool-" + connectionPoolName)
            .maxConnections(webClientConfig.getMaxConnections())
            .pendingAcquireMaxCount(webClientConfig.getPendingAcquireMaxCount())
            .pendingAcquireTimeout(webClientConfig.getResponseTimeout())
            .build();
        var client = HttpClient.create(connectionProvider);
        var clientHttpConnector = new ReactorClientHttpConnector(client);

        return WebClient.builder()
            .baseUrl(path)
            .clientConnector(clientHttpConnector)
            .build();
    }

}
