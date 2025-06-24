package com.gt.bff;

import com.gt.bff.config.ApplicationProperties;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties({ApplicationProperties.class})
@OpenAPIDefinition(
    info = @Info(
        title = "GT BFF API",
        version = "1.0.0",
        description = "Backend For Frontend (BFF) Service for GT Application"
    )
)
public class GtBffApplication {

    public static void main(String[] args) {
        SpringApplication.run(GtBffApplication.class, args);
    }
}
