package com.gt.bff;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@TestConfiguration
@EnableWebMvc
public class TestConfig implements WebMvcConfigurer {
    // Test configuration goes here
}
