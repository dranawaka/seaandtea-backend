package com.seaandtea.oauth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public final class OAuthReturnCookie {

    static final String NAME = "oauth_return";

    private OAuthReturnCookie() {
    }

    public static void write(HttpServletRequest request, HttpServletResponse response, String origin) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(request, encode(origin), Duration.ofMinutes(10)).toString());
    }

    public static void clear(HttpServletRequest request, HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, cookie(request, "", Duration.ZERO).toString());
    }

    public static String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (!NAME.equals(cookie.getName()) || cookie.getValue() == null || cookie.getValue().isBlank()) {
                continue;
            }
            try {
                byte[] decoded = Base64.getUrlDecoder().decode(cookie.getValue());
                return new String(decoded, StandardCharsets.UTF_8);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
        return null;
    }

    private static String encode(String origin) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(origin.getBytes(StandardCharsets.UTF_8));
    }

    private static ResponseCookie cookie(HttpServletRequest request, String value, Duration maxAge) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(request.isSecure())
                .path("/")
                .sameSite("Lax")
                .maxAge(maxAge)
                .build();
    }
}
