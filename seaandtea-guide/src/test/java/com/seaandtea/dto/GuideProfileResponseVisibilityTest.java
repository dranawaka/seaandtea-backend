package com.seaandtea.dto;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GuideProfileResponseVisibilityTest {

    private static ObjectMapper springMapper() {
        return JsonMapper.builder()
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .build();
    }


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

        // Spring Boot turns this off. Unannotated fields, including Spring Data Page
        // metadata, are then omitted whenever a controller method sets a view.
        ObjectMapper mapper = springMapper();
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

    @Test
    void publicPageKeepsGuideListAndHidesContactDetails() throws Exception {
        GuidePageResponse page = GuidePageResponse.builder()
                .content(List.of(GuideProfileResponse.builder()
                        .id(7L)
                        .userFirstName("Nimal")
                        .userEmail("nimal@seaandtea.com")
                        .bio("Tea country walks")
                        .build()))
                .totalPages(3)
                .totalElements(21)
                .number(0)
                .size(10)
                .build();

        ObjectMapper mapper = springMapper();
        String publicJson = mapper.writerWithView(ApiViews.Public.class).writeValueAsString(page);

        assertTrue(publicJson.contains("\"content\""));
        assertTrue(publicJson.contains("Nimal"));
        assertTrue(publicJson.contains("Tea country walks"));
        assertTrue(publicJson.contains("\"totalPages\":3"));
        assertTrue(publicJson.contains("\"totalElements\":21"));
        assertFalse(publicJson.contains("nimal@seaandtea.com"));
        assertFalse(publicJson.contains("userEmail"));
    }
}
