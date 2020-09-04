package com.ftcksu.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "authentication")
public class SecurityProperties {
    private String secretKey;
}
