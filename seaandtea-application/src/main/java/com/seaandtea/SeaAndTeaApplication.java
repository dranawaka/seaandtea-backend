package com.seaandtea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication(scanBasePackages = "com.seaandtea")
@EntityScan("com.seaandtea.entity")
@EnableJpaRepositories("com.seaandtea.repository")
@EnableAsync
@EnableMethodSecurity
public class SeaAndTeaApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SeaAndTeaApplication.class, args);
    }
}

