package com.seaandtea.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class ThingsToDoWebConfig implements WebMvcConfigurer {

    private final ThingsToDoAdminInterceptor adminInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(adminInterceptor).addPathPatterns(
                "/api/v1/admin/activities",
                "/api/v1/admin/activities/**",
                "/api/v1/admin/events",
                "/api/v1/admin/events/**",
                "/api/v1/admin/restaurants",
                "/api/v1/admin/restaurants/**",
                "/api/v1/admin/event-occurrences/**",
                "/api/v1/admin/things-to-do",
                "/api/v1/admin/things-to-do/**",
                "/api/v1/upload/things-to-do/**");
    }
}
