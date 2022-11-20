package com.lllbllllb.web;

import java.util.List;

import com.lllbllllb.common.Entity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import static com.lllbllllb.common.Constants.STRING_STREAM_PATH;

@org.springframework.web.bind.annotation.RestController
@RequiredArgsConstructor
public class RestController {

    private final Service service;

    @PostMapping(STRING_STREAM_PATH)
    public List<Entity> getStringStream(@RequestBody List<String> names) {
        return service.getStringStream(names);
    }
}
