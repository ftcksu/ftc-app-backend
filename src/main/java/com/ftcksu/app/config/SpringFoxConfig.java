package com.ftcksu.app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Collections;

@Configuration
public class SpringFoxConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.ftcksu.app"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "FTC Application",
                "FTC KSU Mobile Application Backend Built Using Spring Boot.",
                "v2.0",
                "https://www.termsofservicegenerator.net/live.php?token=dJeNMzZWDsXorhXvwo9S3JTdfydkLh8E",
                new Contact("Feras Aloudah", "https://github.com/FerasAloudah", "fireslay@gmail.com"),
                "GNU General Public License v3.0", "https://www.gnu.org/licenses/gpl-3.0.en.html", Collections.emptyList());
    }
}
