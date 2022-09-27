package com.lllbllllb.web;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;

import static com.lllbllllb.common.Constants.STRING_SINGLE_PATH;
import static com.lllbllllb.common.Constants.STRING_STREAM_PATH;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {

    private final Service service;

    @GetMapping(STRING_SINGLE_PATH)
    public Map<String, String> getStringSingle() {
        return service.getStringSingle();
    }

    @GetMapping(STRING_STREAM_PATH)
    public List<Map<String, String>> getStringStream() {
        return service.getStringStream();
    }
}
