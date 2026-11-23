package com.seaandtea.oauth;

import com.seaandtea.security.AllowedOrigins;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OAuthIdentityTest {

    @Test
    void readsVerifiedGoogleProfile() {
        OAuthIdentity identity = OAuthIdentity.from("google", Map.of(
                "sub", "google-subject",
                "email", "Ada@Example.com",
                "email_verified", true,
                "given_name", "Ada",
                "family_name", "Lovelace",
                "picture", "https://lh3.googleusercontent.com/a/photo"
        ));

        assertEquals("GOOGLE", identity.storedProvider());
        assertEquals("google-subject", identity.subject());
        assertEquals("ada@example.com", identity.email());
        assertTrue(identity.emailVerified());
        assertEquals("Ada", identity.firstName());
        assertEquals("Lovelace", identity.lastName());
        assertEquals("https://lh3.googleusercontent.com/a/photo", identity.pictureUrl());
    }

    @Test
    void rejectsUnverifiedGoogleEmail() {
        OAuthLoginException error = assertThrows(OAuthLoginException.class, () -> OAuthIdentity.from("google", Map.of(
                "sub", "google-subject",
                "email", "ada@example.com",
                "email_verified", false
        )));
        assertTrue(error.getMessage().contains("did not confirm"));
    }

    @Test
    void splitsFacebookNameAndReadsPicture() {
        OAuthIdentity identity = OAuthIdentity.from("facebook", Map.of(
                "id", "fb-1",
                "name", "Grace Hopper",
                "email", "grace@example.com",
                "picture", Map.of("data", Map.of("url", "https://graph.facebook.com/photo.jpg"))
        ));

        assertEquals("FACEBOOK", identity.storedProvider());
        assertEquals("Grace", identity.firstName());
        assertEquals("Hopper", identity.lastName());
        assertEquals("https://graph.facebook.com/photo.jpg", identity.pictureUrl());
    }

    @Test
    void rejectsUnsafePictureAndForeignReturnOrigin() {
        OAuthIdentity identity = OAuthIdentity.from("facebook", Map.of(
                "id", "fb-2",
                "email", "grace@example.com",
                "first_name", "Grace",
                "picture", "javascript:alert(1)"
        ));
        assertNull(identity.pictureUrl());
        assertEquals("http://localhost:3000", AllowedOrigins.normalizeOrigin("http://localhost:3000"));
        assertEquals("https://www.seaandtea.com", AllowedOrigins.normalizeOrigin("https://www.seaandtea.com/"));
        assertNull(AllowedOrigins.normalizeOrigin("https://evil.example"));
        assertNull(AllowedOrigins.normalizeOrigin("https://www.seaandtea.com/login"));
    }
}
