package com.seaandtea.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideProfileResponseVisibilityTest {

    @Test
    void publicJsonViewOmitsContactAndVerificationFiles() throws Exception {
        GuideProfileResponse response = GuideProfileResponse.builder()
                .id(1L)
                .userFirstName("John")
                .userEmail("guide@seaandtea.com")
                .bio("Public bio")
                .verificationDocuments(List.of("https://files.example/id.pdf"))
                .specialties(List.of(GuideProfileResponse.GuideSpecialtyResponse.builder()
                        .specialty("Tea")
                        .certificationUrl("https://files.example/cert.pdf")
                        .build()))
                .build();

        ObjectMapper mapper = new ObjectMapper();
        String publicJson = mapper.writerWithView(ApiViews.Public.class).writeValueAsString(response);
        assertFalse(publicJson.contains("guide@seaandtea.com"));
        assertFalse(publicJson.contains("userEmail"));
        assertFalse(publicJson.contains("verificationDocuments"));
        assertFalse(publicJson.contains("cert.pdf"));
        assertTrue(publicJson.contains("Public bio"));
        assertTrue(publicJson.contains("Tea"));

        String internalJson = mapper.writerWithView(ApiViews.Internal.class).writeValueAsString(response);
        assertTrue(internalJson.contains("guide@seaandtea.com"));
        assertTrue(internalJson.contains("id.pdf"));
        assertTrue(internalJson.contains("cert.pdf"));

        String ownerJson = mapper.writeValueAsString(response);
        assertTrue(ownerJson.contains("guide@seaandtea.com"));
        assertTrue(ownerJson.contains("id.pdf"));
    }
}
