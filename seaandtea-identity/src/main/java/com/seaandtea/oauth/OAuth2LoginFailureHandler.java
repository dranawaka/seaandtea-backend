package com.seaandtea.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    private final OAuthRedirects redirects;

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception
    ) throws IOException {
        String code = exception instanceof OAuth2AuthenticationException oauth && oauth.getError() != null
                ? oauth.getError().getErrorCode()
                : "unknown";
        log.info("Social sign-in was not completed ({})", code);
        String message = "access_denied".equals(code)
                ? "Sign-in was cancelled."
                : "Social sign-in failed. Please try again.";
        redirects.toApp(request, response, null, message);
    }
}
