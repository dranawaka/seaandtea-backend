package com.seaandtea.oauth;

import java.util.Locale;
import java.util.Map;

public record OAuthIdentity(
        String provider,
        String subject,
        String email,
        boolean emailVerified,
        String firstName,
        String lastName,
        String pictureUrl
) {

    public static OAuthIdentity from(String provider, Map<String, Object> attributes) {
        if (!"google".equals(provider) && !"facebook".equals(provider)) {
            throw new OAuthLoginException("Social sign-in failed. Please try again.");
        }
        Map<String, Object> claims = attributes == null ? Map.of() : attributes;
        String subject = firstText(claims, "google".equals(provider) ? "sub" : "id", "id", "sub");
        if (subject.isBlank() || subject.length() > 255) {
            throw new OAuthLoginException("Social sign-in failed. Please try again.");
        }

        String email = text(claims, "email").toLowerCase(Locale.ROOT);
        boolean verified = "google".equals(provider)
                ? truthy(claims.get("email_verified"))
                : !email.isBlank();
        if (email.isBlank() || email.length() > 255 || !email.contains("@")) {
            throw new OAuthLoginException("facebook".equals(provider)
                    ? "Facebook did not share an email address. Allow email access, or sign up with email."
                    : "The provider did not share an email address.");
        }
        if ("google".equals(provider) && !verified) {
            throw new OAuthLoginException(
                    "Google did not confirm this email address. Use a verified Google account, or sign up with email.");
        }

        String first = firstText(claims, "given_name", "first_name");
        String last = firstText(claims, "family_name", "last_name");
        if (first.isBlank()) {
            String name = text(claims, "name");
            int space = name.indexOf(' ');
            if (!name.isBlank() && space < 0) {
                first = name;
            } else if (space > 0) {
                first = name.substring(0, space);
                if (last.isBlank()) {
                    last = name.substring(space + 1).trim();
                }
            }
        }

        return new OAuthIdentity(
                provider,
                subject,
                email,
                verified,
                cleanName(first, "Traveler"),
                cleanName(last, "Guest"),
                pictureUrl(claims.get("picture"))
        );
    }

    public String storedProvider() {
        return provider.toUpperCase(Locale.ROOT);
    }

    private static String firstText(Map<String, Object> attributes, String... keys) {
        for (String key : keys) {
            String value = text(attributes, key);
            if (!value.isBlank()) {
                return value;
            }
        }
        return "";
    }

    private static String text(Map<String, Object> attributes, String key) {
        Object value = attributes.get(key);
        return value == null ? "" : value.toString().trim();
    }

    private static boolean truthy(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof String text) {
            return "true".equalsIgnoreCase(text) || "1".equals(text);
        }
        return false;
    }

    private static String cleanName(String value, String fallback) {
        String cleaned = value.replaceAll("[\\p{Cntrl}]", "").trim();
        if (cleaned.isBlank()) {
            return fallback;
        }
        return cleaned.length() > 100 ? cleaned.substring(0, 100) : cleaned;
    }

    private static String pictureUrl(Object picture) {
        String url = null;
        if (picture instanceof String value) {
            url = value.trim();
        } else if (picture instanceof Map<?, ?> map) {
            Object data = map.get("data");
            if (data instanceof Map<?, ?> dataMap && dataMap.get("url") instanceof String nested) {
                url = nested.trim();
            } else if (map.get("url") instanceof String direct) {
                url = direct.trim();
            }
        }
        if (url == null || url.length() > 1000 || !url.startsWith("https://")) {
            return null;
        }
        return url;
    }
}
