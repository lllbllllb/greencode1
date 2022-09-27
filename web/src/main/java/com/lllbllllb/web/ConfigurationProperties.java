package com.lllbllllb.web;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
@org.springframework.boot.context.properties.ConfigurationProperties("web-service")
public class ConfigurationProperties {

    private String slowpokeHost = "http://localhost:8080";

}
