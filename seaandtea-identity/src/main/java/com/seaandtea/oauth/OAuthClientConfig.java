package com.seaandtea.oauth;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class OAuthClientConfig {

    @Bean
    @ConfigurationProperties(prefix = "app.oauth")
    public OAuthProperties oauthProperties() {
        return new OAuthProperties();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository(OAuthProperties properties) {
        List<ClientRegistration> registrations = new ArrayList<>();
        if (properties.getGoogle().isConfigured()) {
            registrations.add(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                    .clientId(properties.getGoogle().getClientId())
                    .clientSecret(properties.getGoogle().getClientSecret())
                    .build());
        }
        if (properties.getFacebook().isConfigured()) {
            registrations.add(ClientRegistration.withRegistrationId("facebook")
                    .clientId(properties.getFacebook().getClientId())
                    .clientSecret(properties.getFacebook().getClientSecret())
                    .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                    .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                    .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                    .scope("email", "public_profile")
                    .authorizationUri("https://www.facebook.com/v23.0/dialog/oauth")
                    .tokenUri("https://graph.facebook.com/v23.0/oauth/access_token")
                    .userInfoUri("https://graph.facebook.com/v23.0/me?fields=id,name,email,first_name,last_name,picture")
                    .userNameAttributeName("id")
                    .clientName("Facebook")
                    .build());
        }
        return new ListClientRegistrationRepository(registrations);
    }

    static final class ListClientRegistrationRepository implements ClientRegistrationRepository, Iterable<ClientRegistration> {
        private final Map<String, ClientRegistration> registrations = new LinkedHashMap<>();

        ListClientRegistrationRepository(List<ClientRegistration> registrations) {
            for (ClientRegistration registration : registrations) {
                this.registrations.put(registration.getRegistrationId(), registration);
            }
        }

        @Override
        public ClientRegistration findByRegistrationId(String registrationId) {
            return registrations.get(registrationId);
        }

        @Override
        public Iterator<ClientRegistration> iterator() {
            return registrations.values().iterator();
        }
    }
}
