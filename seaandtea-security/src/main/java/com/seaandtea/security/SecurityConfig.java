package com.seaandtea.security;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] API_DOC_PATHS = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/v3/api-docs.yaml",
            "/v3/api-docs.yml"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final boolean permitApiDocs;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            @Value("${app.api-docs.permit-all:false}") boolean permitApiDocs) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.permitApiDocs = permitApiDocs;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ObjectProvider<ClientRegistrationRepository> clientRegistrations,
            ObjectProvider<AuthenticationSuccessHandler> oauthSuccessHandler,
            ObjectProvider<AuthenticationFailureHandler> oauthFailureHandler
    ) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> {
                // Swagger and the OpenAPI document are not public.
                // Local profile may opt in; otherwise only an admin token can read them.
                if (permitApiDocs) {
                    auth.requestMatchers(API_DOC_PATHS).permitAll();
                } else {
                    auth.requestMatchers(API_DOC_PATHS).hasRole("ADMIN");
                }
                auth.anyRequest().permitAll(); // Method-level @PreAuthorize still enforces role checks
            });
        ClientRegistrationRepository registrations = clientRegistrations.getIfAvailable();
        AuthenticationSuccessHandler successHandler = oauthSuccessHandler.getIfAvailable();
        AuthenticationFailureHandler failureHandler = oauthFailureHandler.getIfAvailable();
        if (registrations instanceof Iterable<?> providers
                && providers.iterator().hasNext()
                && successHandler != null
                && failureHandler != null) {
            http.oauth2Login(oauth2 -> oauth2
                    .successHandler(successHandler)
                    .failureHandler(failureHandler));
        }
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(AllowedOrigins.PATTERNS);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
