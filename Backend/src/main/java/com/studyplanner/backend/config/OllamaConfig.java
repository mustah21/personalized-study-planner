package com.studyplanner.backend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gemini")
@Getter
@Setter
public class OllamaConfig {

    private String baseUrl;
    private String completionUrl;
    private String apiKey;
    private String model;

}
