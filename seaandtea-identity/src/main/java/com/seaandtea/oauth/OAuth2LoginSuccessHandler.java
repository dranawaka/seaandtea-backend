package com.seaandtea.oauth;

import com.seaandtea.dto.AuthResponse;
import com.seaandtea.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;
    private final OAuthRedirects redirects;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)
                || !(authentication.getPrincipal() instanceof OAuth2User oauthUser)) {
            redirects.toApp(request, response, null, "Social sign-in failed. Please try again.");
            return;
        }
        try {
            AuthResponse auth = authService.loginWithOAuth(
                    OAuthIdentity.from(oauthToken.getAuthorizedClientRegistrationId(), oauthUser.getAttributes()));
            log.info("Social sign-in completed for {}", oauthToken.getAuthorizedClientRegistrationId());
            redirects.toApp(request, response, auth.getAccessToken(), null);
        } catch (OAuthLoginException ex) {
            redirects.toApp(request, response, null, ex.getMessage());
        } catch (RuntimeException ex) {
            log.error("Social sign-in failed for {}", oauthToken.getAuthorizedClientRegistrationId(), ex);
            redirects.toApp(request, response, null, "Social sign-in failed. Please try again.");
        }
    }
}
