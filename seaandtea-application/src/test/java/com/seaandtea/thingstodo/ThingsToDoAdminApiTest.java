package com.seaandtea.thingstodo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seaandtea.entity.Event;
import com.seaandtea.entity.User;
import com.seaandtea.repository.EventRepository;
import com.seaandtea.repository.UserRepository;
import com.seaandtea.security.JwtService;
import com.seaandtea.service.FileUploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "things-to-do.seed.enabled=true")
class ThingsToDoAdminApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository users;

    @Autowired
    private EventRepository events;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private FileUploadService fileUploadService;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void users() {
        when(fileUploadService.uploadThingImage(any(), anyString(), anyLong(), anyString()))
                .thenReturn("https://cdn.example/thing.webp");
        adminToken = token("admin-ttd@example.com", User.UserRole.ADMIN);
        userToken = token("user-ttd@example.com", User.UserRole.USER);
    }

    @Test
    void adminCreatesRejectsDuplicatesAndRequiresASourceToPublish() throws Exception {
        String body = """
                {"slug":"admin-test-hike","title":"Admin test hike","destinationSlug":"ella","category":"HIKING","region":"HILL_COUNTRY","summary":"A short test hike"}
                """;
        mockMvc.perform(post("/api/v1/admin/activities").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/admin/activities").header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());

        String created = mockMvc.perform(post("/api/v1/admin/activities").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("admin-test-hike"))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(created).get("id").asLong();

        mockMvc.perform(post("/api/v1/admin/activities").header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());

        mockMvc.perform(patch("/api/v1/admin/activities/" + id + "/status")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"PUBLISHED\",\"markVerified\":true}"))
                .andExpect(status().isBadRequest());

        MockMultipartFile file = new MockMultipartFile("file", "hike.jpg", "image/jpeg", new byte[]{1, 2, 3});
        mockMvc.perform(multipart("/api/v1/upload/things-to-do/activities/" + id + "/image").file(file)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("https://cdn.example/thing.webp"));
    }

    @Test
    void confirmedOccurrenceNeedsAStartDate() throws Exception {
        Event event = events.findBySlug("kandy-esala-perahera").orElseThrow();
        mockMvc.perform(post("/api/v1/admin/events/" + event.getId() + "/occurrences")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2030,\"dateStatus\":\"CONFIRMED\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/v1/admin/events/" + event.getId() + "/occurrences")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"year\":2030,\"dateStatus\":\"CONFIRMED\",\"startDate\":\"2030-08-01\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.year").value(2030));
    }

    @Test
    void draftsStayOutOfThePublicList() throws Exception {
        JsonNode list = objectMapper.readTree(mockMvc.perform(get("/api/v1/admin/activities")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("status", "DRAFT"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString());
        boolean found = false;
        for (JsonNode row : list.get("content")) {
            if ("tea-estate-visits".equals(row.get("slug").asText())) {
                found = true;
            }
        }
        org.junit.jupiter.api.Assertions.assertTrue(found);
    }

    private String token(String email, User.UserRole role) {
        User user = users.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setEmail(email);
            created.setPassword("test");
            created.setFirstName("Ada");
            created.setLastName("Admin");
            created.setRole(role);
            created.setIsActive(true);
            created.setIsVerified(true);
            created.setAuthProvider("LOCAL");
            return users.save(created);
        });
        return jwtService.generateTokenWithUserId(user, user.getId());
    }
}
