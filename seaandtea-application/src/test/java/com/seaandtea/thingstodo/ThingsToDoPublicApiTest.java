package com.seaandtea.thingstodo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaandtea.entity.Activity;
import com.seaandtea.entity.Guide;
import com.seaandtea.entity.Tour;
import com.seaandtea.entity.User;
import com.seaandtea.repository.ActivityRepository;
import com.seaandtea.repository.GuideRepository;
import com.seaandtea.repository.TourRepository;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "things-to-do.seed.enabled=true")
class ThingsToDoPublicApiTest {

    private static final Set<String> FORBIDDEN = Set.of(
            "email", "userEmail", "verificationDocuments", "needsVerification",
            "moderationNote", "submittedByUserId", "reservationsEmail");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository users;

    @Autowired
    private GuideRepository guides;

    @Autowired
    private TourRepository tours;

    @Autowired
    private ActivityRepository activities;

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void toursForLinking() {
        if (tours.findAll().stream().anyMatch(tour -> "Mirissa Whale and Beach".equals(tour.getTitle()))) {
            return;
        }
        User user = new User();
        user.setEmail("guide-ttd@example.com");
        user.setPassword("test");
        user.setFirstName("Nimal");
        user.setLastName("Perera");
        user.setRole(User.UserRole.GUIDE);
        user.setIsActive(true);
        user.setIsVerified(true);
        user.setAuthProvider("LOCAL");
        user = users.save(user);

        Guide guide = new Guide();
        guide.setUser(user);
        guide.setVerificationStatus(Guide.VerificationStatus.VERIFIED);
        guide.setAverageRating(new BigDecimal("4.80"));
        guide.setBio("Coast guide");
        guide = guides.save(guide);

        saveTour(guide, "Mirissa Whale and Beach", "Mirissa", Tour.TourCategory.BEACH_TOURS, "120.00");
        saveTour(guide, "Yala Leopard Safari", "Yala", Tour.TourCategory.WILDLIFE_TOURS, "135.00");
        saveTour(guide, "Zebra Explicit Tour", "Mirissa", Tour.TourCategory.BEACH_TOURS, "200.00");
        saveTour(guide, "Test", "Mirissa", Tour.TourCategory.BEACH_TOURS, "1.00");
    }

    @Test
    void julySeasonFlagsAndVisibility() throws Exception {
        JsonNode body = json(mockMvc.perform(get("/api/v1/activities")
                        .param("month", "7")
                        .param("inSeason", "false")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn());
        Map<String, Boolean> inSeason = new java.util.HashMap<>();
        body.get("content").forEach(card -> inSeason.put(card.get("slug").asText(), card.get("inSeasonNow").asBoolean()));
        assertTrue(inSeason.get("arugam-bay-surfing"));
        assertTrue(inSeason.get("trincomalee-whale-watching"));
        assertFalse(inSeason.get("weligama-surfing"));
        assertFalse(inSeason.get("mirissa-whale-watching"));
        assertFalse(body.toString().contains("tea-estate-visits"));
        assertNoForbidden(body);

        JsonNode onlyInSeason = json(mockMvc.perform(get("/api/v1/activities")
                        .param("month", "7")
                        .param("inSeason", "true")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andReturn());
        Set<String> slugs = new HashSet<>();
        onlyInSeason.get("content").forEach(card -> slugs.add(card.get("slug").asText()));
        assertTrue(slugs.contains("arugam-bay-surfing"));
        assertFalse(slugs.contains("weligama-surfing"));
        assertFalse(slugs.contains("mirissa-whale-watching"));
    }

    @Test
    void unknownSlugIs404AndBadEnumIs400AndDraftIsHidden() throws Exception {
        mockMvc.perform(get("/api/v1/activities/not-a-real-place")).andExpect(status().isNotFound());
        mockMvc.perform(get("/api/v1/activities").param("category", "NOT_A_CATEGORY")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/api/v1/activities/tea-estate-visits")).andExpect(status().isNotFound());
    }

    @Test
    void linkingPrefersExplicitToursAndDropsTestTitles() throws Exception {
        Activity whales = activities.findBySlug("mirissa-whale-watching").orElseThrow();
        Tour zebra = tours.findAll().stream().filter(tour -> "Zebra Explicit Tour".equals(tour.getTitle())).findFirst().orElseThrow();
        User guide = users.findByEmail("guide-ttd@example.com").orElseThrow();
        String token = jwtService.generateTokenWithUserId(guide, guide.getId());

        mockMvc.perform(put("/api/v1/tours/" + zebra.getId() + "/things-to-do")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[{\"type\":\"ACTIVITY\",\"id\":" + whales.getId() + "}]"))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/v1/tours/" + zebra.getId() + "/things-to-do")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[]"))
                .andExpect(status().isUnauthorized());

        JsonNode linked = json(mockMvc.perform(get("/api/v1/activities/mirissa-whale-watching/tours").param("size", "6"))
                .andExpect(status().isOk())
                .andReturn());
        assertEquals("Zebra Explicit Tour", linked.get("tours").get(0).get("title").asText());
        assertTrue(titles(linked).contains("Mirissa Whale and Beach"));
        assertFalse(titles(linked).contains("Test"));
        assertEquals("Nimal", linked.get("guides").get(0).get("firstName").asText());
        assertFalse(linked.get("guides").get(0).has("lastName"));
        assertNoForbidden(linked);

        JsonNode yala = json(mockMvc.perform(get("/api/v1/activities/yala-safari/tours"))
                .andExpect(status().isOk()).andReturn());
        assertTrue(titles(yala).contains("Yala Leopard Safari"));

        JsonNode arugam = json(mockMvc.perform(get("/api/v1/activities/arugam-bay-surfing/tours"))
                .andExpect(status().isOk()).andReturn());
        assertEquals(0, arugam.get("tours").size());

        JsonNode restaurant = json(mockMvc.perform(get("/api/v1/restaurants/ministry-of-crab"))
                .andExpect(status().isOk()).andReturn());
        assertNoForbidden(restaurant);
    }

    private void saveTour(Guide guide, String title, String destination, Tour.TourCategory category, String price) {
        Tour tour = new Tour();
        tour.setGuide(guide);
        tour.setTitle(title);
        tour.setCategory(category);
        tour.setDestination(destination);
        tour.setDurationHours(8);
        tour.setPricePerPerson(new BigDecimal(price));
        tour.setIsActive(true);
        tour.setDescription(title);
        tours.save(tour);
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Set<String> titles(JsonNode body) {
        Set<String> titles = new HashSet<>();
        body.get("tours").forEach(tour -> titles.add(tour.get("title").asText()));
        return titles;
    }

    private void assertNoForbidden(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }
        if (node.isObject()) {
            Iterator<String> names = node.fieldNames();
            while (names.hasNext()) {
                String name = names.next();
                assertFalse(FORBIDDEN.contains(name), name);
                assertNoForbidden(node.get(name));
            }
        } else if (node.isArray()) {
            node.forEach(this::assertNoForbidden);
        }
    }
}
