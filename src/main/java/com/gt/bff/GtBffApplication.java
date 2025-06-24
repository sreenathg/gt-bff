package com.gt.bff;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"com.gt", "com.gt.bff"})
@EnableAsync
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
