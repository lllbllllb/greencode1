package com.lllbllllb.loader;

import java.time.Clock;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import static com.lllbllllb.common.Constants.STRING_STREAM_PATH;

@SpringBootApplication
public class LoaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoaderApplication.class, args);
    }

    @Bean
    static HandlerAdapter wsHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    static HandlerMapping webSocketMapping(LoaderWebSocketHandler loaderWebSocketHandler) {
        var map = Map.of("/websocket/load", loaderWebSocketHandler);
        var simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);
        simpleUrlHandlerMapping.setOrder(10);

        return simpleUrlHandlerMapping;
    }

    @Bean
    static Map<String, WebClient> serviceNameToWebClientMap(ConfigurationProperties properties) {
        var webClientConfig = properties.getWebClientConfig();
        // https://stackoverflow.com/a/68658096
        var connectionProvider = ConnectionProvider.builder("customConnectionPool")
            .maxConnections(webClientConfig.getMaxConnections())
            .pendingAcquireMaxCount(webClientConfig.getPendingAcquireMaxCount())
            .build();
        var client = HttpClient.create(connectionProvider)
            .responseTimeout(webClientConfig.getResponseTimeout());
        var clientHttpConnector = new ReactorClientHttpConnector(client);

        return properties.getServices().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry -> {
                var host = entry.getValue().getHost();

                return WebClient.builder()
                    .baseUrl(host + STRING_STREAM_PATH)
                    .clientConnector(clientHttpConnector)
                    .build();
            }));
    }

    @Bean
    static Clock clock() {
        return Clock.systemDefaultZone();
    }

}
