package com.seaandtea.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl("http://localhost:8080");
        devServer.setDescription("Server URL in Development environment");
        
        Server prodServer = new Server();
        prodServer.setUrl("https://api.seaandtea.com");
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
        
        return new OpenAPI().info(info).servers(List.of(devServer, prodServer));
    }
}

