package com.seaandtea.security;

import org.springframework.web.cors.CorsConfiguration;

import java.net.URI;
import java.util.List;

public final class AllowedOrigins {

    public static final List<String> PATTERNS = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://www.seaandtea.com",
            "https://seaandtea.com",
            "https://*.vercel.app",
            "https://*.railway.app",
            "https://*.onrender.com"
    );

    private AllowedOrigins() {
    }

    /**
     * Returns a scheme-host-port origin when it matches the public app hosts, otherwise null.
     */
    public static String normalizeOrigin(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.indexOf('\r') >= 0 || trimmed.indexOf('\n') >= 0) {
            return null;
        }
        final URI uri;
        try {
            uri = URI.create(trimmed);
        } catch (IllegalArgumentException ex) {
            return null;
        }
        if (uri.getScheme() == null || uri.getHost() == null || uri.getUserInfo() != null) {
            return null;
        }
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return null;
        }
        if (uri.getPath() != null && !uri.getPath().isEmpty() && !"/".equals(uri.getPath())) {
            return null;
        }
        String scheme = uri.getScheme().toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            return null;
        }
        String origin = scheme + "://" + uri.getHost().toLowerCase();
        if (uri.getPort() != -1) {
            origin += ":" + uri.getPort();
        }
        return allows(origin) ? origin : null;
    }

    public static boolean allows(String origin) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(PATTERNS);
        return configuration.checkOrigin(origin) != null;
    }
}
