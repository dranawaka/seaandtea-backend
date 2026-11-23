package com.seaandtea.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuthRedirects {

    private final OAuthReturnTargets targets;

    public void toApp(HttpServletRequest request, HttpServletResponse response, String token, String error) throws IOException {
        String origin = targets.resolve(request);
        OAuthReturnCookie.clear(request, response);
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        StringBuilder location = new StringBuilder(origin).append("/auth/callback#");
        if (token != null) {
            location.append("token=").append(URLEncoder.encode(token, StandardCharsets.UTF_8));
        } else {
            String message = error == null || error.isBlank()
                    ? "Social sign-in failed. Please try again."
                    : error;
            if (message.length() > 200) {
                message = message.substring(0, 200);
            }
            location.append("error=").append(URLEncoder.encode(message, StandardCharsets.UTF_8));
        }
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        response.sendRedirect(location.toString());
    }
}
