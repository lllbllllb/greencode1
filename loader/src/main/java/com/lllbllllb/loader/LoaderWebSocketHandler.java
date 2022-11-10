package com.lllbllllb.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoaderWebSocketHandler implements WebSocketHandler {

    private final LoadService loadService;

    private final ObjectMapper objectMapper;

    @NonNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        var serviceName = session.getHandshakeInfo().getUri().getQuery();

        if (serviceName == null) {
            throw new IllegalArgumentException("No serviceName present");
        }

        var out = loadService.getLoadEventStream(serviceName)
            .map(this::toJson)
            .map(session::textMessage)
            .as(session::send);

        return session.receive()
            .doOnNext(webSocketMessage -> {
                var json = webSocketMessage.getPayloadAsText();
                var incomeEvent = fromJson(json);

                loadService.receiveEvent(serviceName, incomeEvent);
            })
            .doOnError(err -> log.error(err.getMessage(), err))
            .doFinally(signal -> loadService.finalize(serviceName))
            .zipWith(out)
            .then();
    }

    @SneakyThrows
    private IncomeEvent fromJson(String json) {
        return objectMapper.readValue(json, IncomeEvent.class);
    }

    @SneakyThrows
    private String toJson(AttemptReport attemptReport) {
        return objectMapper.writeValueAsString(attemptReport);
    }

}
