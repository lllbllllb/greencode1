package com.lllbllllb.loader;

import java.util.Map;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@org.springframework.boot.context.properties.ConfigurationProperties("loader-service")
public class ConfigurationProperties {

    private Map<String, Service> services;

    @Data
    public static class Service {

        private String host;

    }

}
