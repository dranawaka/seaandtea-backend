package com.seaandtea.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import jakarta.annotation.PostConstruct;

@Configuration
public class LoggingConfig {
    
    private final Environment environment;
    
    public LoggingConfig(Environment environment) {
        this.environment = environment;
    }
    
    @PostConstruct
    public void configureLogging() {
        // Set system properties to prevent message truncation
        System.setProperty("spring.output.ansi.enabled", "always");
        System.setProperty("spring.web.log-request-details", "true");
        System.setProperty("spring.web.log-response-details", "true");
        
        // Set logging level properties
        System.setProperty("logging.level.org.springframework.web", "DEBUG");
        System.setProperty("logging.level.org.springframework.web.servlet.mvc.method.annotation.HttpEntityMethodProcessor", "DEBUG");
        System.setProperty("logging.level.org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor", "DEBUG");
        System.setProperty("logging.level.com.fasterxml.jackson", "DEBUG");
        System.setProperty("logging.level.com.seaandtea", "DEBUG");
    }
}
