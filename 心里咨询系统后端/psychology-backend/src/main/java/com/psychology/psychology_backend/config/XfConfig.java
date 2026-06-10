package com.psychology.psychology_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "xf")
public class XfConfig {
    private String appid;
    private String apiSecret;
    private String apiKey;
    private String hostUrl;
}