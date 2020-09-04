package com.ftcksu.app;

import com.ftcksu.app.config.FirebaseProperties;
import com.ftcksu.app.config.SecurityProperties;
import com.ftcksu.app.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, FirebaseProperties.class, SecurityProperties.class})
public class FtcBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(FtcBackendApplication.class, args);
    }

}
