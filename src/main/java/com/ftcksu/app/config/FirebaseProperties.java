package com.ftcksu.app.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {
    private String configurationFile;
}
