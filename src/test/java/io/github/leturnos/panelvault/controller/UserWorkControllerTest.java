package io.github.leturnos.panelvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leturnos.panelvault.config.TokenService;
import io.github.leturnos.panelvault.dto.UserWorkRequestDTO;
import io.github.leturnos.panelvault.dto.UserWorkResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.repository.UserRepository;
import io.github.leturnos.panelvault.service.UserWorkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserWorkController.class)
@WithMockUser
class UserWorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserWorkService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("PUT /works/{workId}/collection tests")
    class SaveOrUpdateTests {

        @Test
        @DisplayName("Should return 200 OK when payload has valid status and step-0.5 rating")
        void saveOrUpdate_shouldReturn200_whenPayloadIsValid() throws Exception {
            UserWorkRequestDTO request = new UserWorkRequestDTO(WorkStatus.ONGOING, new BigDecimal("8.5"));
            UserWorkResponseDTO response = new UserWorkResponseDTO(1L, 10L, "Berserk", WorkStatus.ONGOING, new BigDecimal("8.5"));

            Mockito.when(service.saveOrUpdate(eq(10L), any(UserWorkRequestDTO.class), any()))
                    .thenReturn(response);

            mockMvc.perform(put("/works/10/collection")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.workId", is(10)))
                    .andExpect(jsonPath("$.status", is("ONGOING")))
                    .andExpect(jsonPath("$.rating", is(8.5)));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when rating is not in 0.5 steps (custom @AssertTrue validation)")
        void saveOrUpdate_shouldReturn400_whenRatingStepIsInvalid() throws Exception {
            // 8.3 is not divisible by 0.5
            UserWorkRequestDTO request = new UserWorkRequestDTO(WorkStatus.ONGOING, new BigDecimal("8.3"));

            mockMvc.perform(put("/works/10/collection")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].message", hasItem(containsString("passos de 0.5"))));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when collection status is null")
        void saveOrUpdate_shouldReturn400_whenStatusIsNull() throws Exception {
            mockMvc.perform(put("/works/10/collection")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "status": null,
                                      "rating": 8.0
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("status")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when rating exceeds 10.0")
        void saveOrUpdate_shouldReturn400_whenRatingExceedsMaximum() throws Exception {
            UserWorkRequestDTO request = new UserWorkRequestDTO(WorkStatus.ONGOING, new BigDecimal("10.5"));

            mockMvc.perform(put("/works/10/collection")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("rating")));
        }
    }

    @Nested
    @DisplayName("GET & DELETE /works/{workId}/collection tests")
    class FindAndDeleteTests {

        @Test
        @DisplayName("Should return 200 OK with UserWork details when work exists in user collection")
        void findByWorkIdAndUser_shouldReturn200_whenPresent() throws Exception {
            UserWorkResponseDTO response = new UserWorkResponseDTO(1L, 10L, "Berserk", WorkStatus.COMPLETED, new BigDecimal("10.0"));

            Mockito.when(service.findByWorkIdAndUser(eq(10L), any()))
                    .thenReturn(response);

            mockMvc.perform(get("/works/10/collection"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.workId", is(10)))
                    .andExpect(jsonPath("$.status", is("COMPLETED")));
        }

        @Test
        @DisplayName("Should return 204 No Content when work is removed from collection")
        void delete_shouldReturn204_whenSuccessful() throws Exception {
            mockMvc.perform(delete("/works/10/collection")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            Mockito.verify(service).delete(eq(10L), any());
        }
    }
}
