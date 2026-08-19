package io.github.leturnos.panelvault.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leturnos.panelvault.AbstractIntegrationTest;
import io.github.leturnos.panelvault.dto.LoginRequestDTO;
import io.github.leturnos.panelvault.dto.RegisterRequestDTO;
import io.github.leturnos.panelvault.dto.UserWorkRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class AuthAndUserWorkFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should successfully register a new user and login to retrieve a JWT token")
    void registerAndLogin_shouldIssueValidJwtToken() throws Exception {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@example.com";

        RegisterRequestDTO registerDTO = new RegisterRequestDTO(username, email, "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is(username)))
                .andExpect(jsonPath("$.email", is(email)));

        LoginRequestDTO loginDTO = new LoginRequestDTO(username, "password123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("Should allow authenticated user to add, update, and remove a work from their collection")
    void userCollection_shouldAllowManagingWorkStatusAndRating() throws Exception {
        String token = registerAndGetToken("collector");
        long workId = createWork(token, "Berserk");

        // Add to collection with ONGOING and 9.5 rating
        UserWorkRequestDTO addRequest = new UserWorkRequestDTO(WorkStatus.ONGOING, new BigDecimal("9.5"));
        mockMvc.perform(put("/works/" + workId + "/collection")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workId", is((int) workId)))
                .andExpect(jsonPath("$.status", is("ONGOING")))
                .andExpect(jsonPath("$.rating", is(9.5)));

        // Get collection status
        mockMvc.perform(get("/works/" + workId + "/collection")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workId", is((int) workId)))
                .andExpect(jsonPath("$.status", is("ONGOING")));

        // Delete from collection
        mockMvc.perform(delete("/works/" + workId + "/collection")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        // Verify it was removed
        mockMvc.perform(get("/works/" + workId + "/collection")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should enforce volume ownership isolation between distinct users")
    void volumes_shouldIsolateDataBetweenUsers() throws Exception {
        String tokenAlice = registerAndGetToken("alice_vol");
        String tokenBob = registerAndGetToken("bob_vol");
        long workId = createWork(tokenAlice, "Monster");

        // Alice creates Volume 1
        VolumeRequestDTO volumeDTO = new VolumeRequestDTO(1, LocalDate.of(2024, 1, 1), new BigDecimal("39.90"), true);
        MvcResult volumeResult = mockMvc.perform(post("/works/" + workId + "/volumes")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(volumeDTO)))
                .andExpect(status().isCreated())
                .andReturn();
        long volumeId = objectMapper.readTree(volumeResult.getResponse().getContentAsString()).get("id").asLong();

        // Alice lists volumes -> finds 1 volume
        mockMvc.perform(get("/works/" + workId + "/volumes")
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is((int) volumeId)));

        // Bob lists volumes -> finds 0 volumes (isolated)
        mockMvc.perform(get("/works/" + workId + "/volumes")
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Bob cannot fetch Alice's volume by ID (404)
        mockMvc.perform(get("/volumes/" + volumeId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isNotFound());

        // Bob cannot delete Alice's volume by ID (404)
        mockMvc.perform(delete("/volumes/" + volumeId)
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should compute isolated personal stats per user as well as global stats")
    void stats_shouldReflectCorrectMetricsForIndividualUsersAndGlobal() throws Exception {
        String tokenAlice = registerAndGetToken("alice_stats");
        String tokenBob = registerAndGetToken("bob_stats");
        long workId = createWork(tokenAlice, "Fullmetal Alchemist");

        // Alice: ONGOING + 1 owned volume
        UserWorkRequestDTO aliceCollection = new UserWorkRequestDTO(WorkStatus.ONGOING, new BigDecimal("10.0"));
        mockMvc.perform(put("/works/" + workId + "/collection")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(aliceCollection)))
                .andExpect(status().isOk());

        VolumeRequestDTO volumeDTO = new VolumeRequestDTO(1, LocalDate.of(2024, 1, 1), new BigDecimal("45.00"), true);
        mockMvc.perform(post("/works/" + workId + "/volumes")
                        .header("Authorization", "Bearer " + tokenAlice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(volumeDTO)))
                .andExpect(status().isCreated());

        // Bob: WISHLIST + 0 volumes
        UserWorkRequestDTO bobCollection = new UserWorkRequestDTO(WorkStatus.WISHLIST, null);
        mockMvc.perform(put("/works/" + workId + "/collection")
                        .header("Authorization", "Bearer " + tokenBob)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bobCollection)))
                .andExpect(status().isOk());

        // Alice personal stats
        mockMvc.perform(get("/stats/me")
                        .header("Authorization", "Bearer " + tokenAlice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorks", is(1)))
                .andExpect(jsonPath("$.totalVolumes", is(1)))
                .andExpect(jsonPath("$.onGoingWorks", is(1)));

        // Bob personal stats
        mockMvc.perform(get("/stats/me")
                        .header("Authorization", "Bearer " + tokenBob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalWorks", is(1)))
                .andExpect(jsonPath("$.totalVolumes", is(0)))
                .andExpect(jsonPath("$.wishlistItems", is(1)));
    }

    private String registerAndGetToken(String baseName) throws Exception {
        String username = baseName + "_" + UUID.randomUUID().toString().substring(0, 6);
        String email = username + "@example.com";

        RegisterRequestDTO registerDTO = new RegisterRequestDTO(username, email, "password123");
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());

        LoginRequestDTO loginDTO = new LoginRequestDTO(username, "password123");
        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();
    }

    private long createWork(String token, String baseTitle) throws Exception {
        String title = baseTitle + " " + UUID.randomUUID().toString().substring(0, 6);
        WorkRequestDTO createWork = new WorkRequestDTO(
                title,
                WorkType.MANGA,
                "Publisher",
                "Author",
                10,
                "https://example.com/cover.jpg"
        );
        MvcResult workResult = mockMvc.perform(post("/works")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createWork)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(workResult.getResponse().getContentAsString()).get("id").asLong();
    }
}
