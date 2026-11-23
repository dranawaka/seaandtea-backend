package com.seaandtea.oauth;

import com.seaandtea.security.AllowedOrigins;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuthReturnTargets {

    private final String frontendUrl;

    public OAuthReturnTargets(@Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.frontendUrl = frontendUrl;
    }

    public String configuredOrigin() {
        String normalized = AllowedOrigins.normalizeOrigin(frontendUrl);
        return normalized != null ? normalized : "http://localhost:3000";
    }

    public String resolve(HttpServletRequest request) {
        Object preset = request.getAttribute("oauth.return");
        if (preset instanceof String value) {
            String normalized = AllowedOrigins.normalizeOrigin(value);
            if (normalized != null) {
                return normalized;
            }
        }
        String fromCookie = AllowedOrigins.normalizeOrigin(OAuthReturnCookie.read(request));
        return fromCookie != null ? fromCookie : configuredOrigin();
    }
}
