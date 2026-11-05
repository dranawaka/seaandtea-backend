package com.seaandtea.config;

import com.seaandtea.interceptor.RequestResponseLoggingInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final RequestResponseLoggingInterceptor requestResponseLoggingInterceptor;
    
    public WebMvcConfig(RequestResponseLoggingInterceptor requestResponseLoggingInterceptor) {
        this.requestResponseLoggingInterceptor = requestResponseLoggingInterceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestResponseLoggingInterceptor)
                .addPathPatterns("/**") // Apply to all paths
                .excludePathPatterns("/actuator/**", "/error"); // Exclude actuator and error endpoints
    }
}

