package com.lllbllllb.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ObjectMapperService {

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public LoadConfiguration fromJson(String json) {
        return objectMapper.readValue(json, LoadConfiguration.class);
    }

    @SneakyThrows
    public String toJson(AttemptResult attemptResult) {
        return objectMapper.writeValueAsString(attemptResult);
    }

}
