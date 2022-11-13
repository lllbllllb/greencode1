package com.lllbllllb.loader;

import java.time.Duration;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import reactor.util.function.Tuple2;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class LoadUnitFactory {

    public Mono<Tuple2<Long, Integer>> create(Prey prey) {
        var connectionProvider = ConnectionProvider.builder("loaderConnectionPool-" + prey.name())
            .pendingAcquireMaxCount(-1) // no limit
            .build();
        var client = HttpClient.create(connectionProvider)
            .responseTimeout(Duration.ofMillis(prey.timeoutMs()));
        var clientHttpConnector = new ReactorClientHttpConnector(client);
        var headers = prey.headers().entrySet().stream()
            .collect(collectingAndThen(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())), MultiValueMapAdapter::new));

        return WebClient.builder()
            .defaultHeaders(httpHeaders -> httpHeaders.addAll(headers))
            .baseUrl("%s?%s".formatted(prey.path(), prey.requestParameters()))
            .clientConnector(clientHttpConnector)
            .build()
            .method(prey.method())
            .bodyValue(prey.requestBody())
            .exchangeToMono(clientResponse -> Mono.just(clientResponse.rawStatusCode()))
            .elapsed();
    }

}
