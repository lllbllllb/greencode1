package com.lllbllllb.loader;

import java.time.Duration;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class WebClientFactory {

    public RequestHeadersSpec<?> create(Prey prey) {
        var connectionProvider = ConnectionProvider.builder("loaderConnectionPool-" + prey.getName())
            .pendingAcquireMaxCount(-1) // no limit
            .build();
        var client = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofMillis(prey.getTimeoutMs()));
        var clientHttpConnector = new ReactorClientHttpConnector(client);
        var headers = prey.getHeaders().entrySet().stream()
            .collect(collectingAndThen(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())), MultiValueMapAdapter::new));

        return WebClient.builder()
            .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
            .baseUrl("%s?%s".formatted(prey.getPath(), prey.getRequestParameters()))
            .clientConnector(clientHttpConnector)
            .build()
            .method(prey.getMethod())
            .bodyValue(prey.getRequestBody());
    }

}
