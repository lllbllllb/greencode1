package com.lllbllllb.webflux;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.lllbllllb.common.Constants.STRING_SINGLE_PATH;
import static com.lllbllllb.common.Constants.STRING_STREAM_PATH;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {

    private final Service service;

    @GetMapping(STRING_SINGLE_PATH)
    public Mono<Map<String, String>> getStringSingle() {
        return service.getStringSingle();
    }

    @GetMapping(STRING_STREAM_PATH)
    public Flux<Map<String, String>> getStringStream() {
        return service.getStringStream();
    }

    @GetMapping(value = STRING_STREAM_PATH + 1, produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Map<String, String>> getStringStream1() {
        return service.getStringStream();
    }

}
