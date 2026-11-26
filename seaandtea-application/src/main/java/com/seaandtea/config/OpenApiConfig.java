package com.seaandtea.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080/api/v1");
        devServer.setDescription("Server URL in Development environment");
        
        Server prodServer = new Server();
        prodServer.setUrl("https://api.seaandtea.com/api/v1");
        prodServer.setDescription("Server URL in Production environment");
        
        Contact contact = new Contact();
        contact.setEmail("info@seaandtea.com");
        contact.setName("Sea & Tea Tours");
        contact.setUrl("https://www.seaandtea.com");
        
        License mitLicense = new License().name("MIT License").url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
                .title("Sea & Tea Tours API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints for Sea & Tea Tours platform - The Upwork for Travel Guides in Sri Lanka")
                .termsOfService("https://www.seaandtea.com/terms")
                .license(mitLicense);
        
        SecurityScheme bearerAuth = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("JWT access token from the login endpoint");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer, prodServer))
                .components(new Components().addSecuritySchemes("bearerAuth", bearerAuth));
    }

    /**
     * Mark operations that use method security as requiring the bearer token.
     * Public operations, including those annotated permitAll, stay unmarked.
     */
    @Bean
    public OperationCustomizer bearerSecurityCustomizer() {
        return (operation, handlerMethod) -> {
            PreAuthorize methodSecurity = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getMethod(), PreAuthorize.class);
            if (methodSecurity == null) {
                methodSecurity = AnnotatedElementUtils.findMergedAnnotation(
                        handlerMethod.getBeanType(), PreAuthorize.class);
            }
            if (methodSecurity == null || methodSecurity.value().contains("permitAll")) {
                return operation;
            }
            boolean alreadyMarked = operation.getSecurity() != null && operation.getSecurity().stream()
                    .anyMatch(requirement -> requirement.containsKey("bearerAuth"));
            if (!alreadyMarked) {
                operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
            }
            return operation;
        };
    }
}

