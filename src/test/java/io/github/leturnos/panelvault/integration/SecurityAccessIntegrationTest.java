package io.github.leturnos.panelvault.integration;

import io.github.leturnos.panelvault.AbstractIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SecurityAccessIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Public endpoint accessibility tests")
    class PublicEndpoints {

        @Test
        @DisplayName("GET /stats should be publicly accessible without authentication")
        void getStats_shouldReturn200_withoutAuth() throws Exception {
            mockMvc.perform(get("/stats"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /auth/register should be publicly accessible without authentication")
        void register_shouldBeAccessible_withoutAuth() throws Exception {
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest()); // 400 means route is accessible and hit validation
        }

        @Test
        @DisplayName("POST /auth/login should be publicly accessible without authentication")
        void login_shouldBeAccessible_withoutAuth() throws Exception {
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest()); // 400 means route is accessible and hit validation
        }

        @Test
        @DisplayName("GET /v3/api-docs should be publicly accessible")
        void apiDocs_shouldReturn200_withoutAuth() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /swagger-ui/index.html should be publicly accessible")
        void swaggerUi_shouldReturn200_withoutAuth() throws Exception {
            mockMvc.perform(get("/swagger-ui/index.html"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected endpoint access restriction tests")
    class ProtectedEndpoints {

        @Test
        @DisplayName("GET /stats/me should return 401 when unauthenticated")
        void getMyStats_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(get("/stats/me"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /works/1/collection should return 401 when unauthenticated")
        void getUserCollection_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(get("/works/1/collection"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /works/1/collection should return 401 when unauthenticated")
        void putUserCollection_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(put("/works/1/collection")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /works/1/collection should return 401 when unauthenticated")
        void deleteUserCollection_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/works/1/collection"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("GET /volumes/1 should return 401 when unauthenticated")
        void getVolume_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(get("/volumes/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /works/1/volumes should return 401 when unauthenticated")
        void postVolume_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(post("/works/1/volumes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /volumes/1 should return 401 when unauthenticated")
        void deleteVolume_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/volumes/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("POST /works should return 401 when unauthenticated")
        void postWork_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(post("/works")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("PUT /works/1 should return 401 when unauthenticated")
        void putWork_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(put("/works/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("DELETE /works/1 should return 401 when unauthenticated")
        void deleteWork_shouldReturn401_whenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/works/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
