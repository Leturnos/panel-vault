package io.github.leturnos.panelvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leturnos.panelvault.config.TokenService;
import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeResponseDTO;
import io.github.leturnos.panelvault.repository.UserRepository;
import io.github.leturnos.panelvault.service.VolumeService;
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
import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VolumeController.class)
@WithMockUser
class VolumeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VolumeService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("POST /works/{id}/volumes tests")
    class CreateTests {

        @Test
        @DisplayName("Should return 201 Created with Location header when volume payload is valid")
        void create_shouldReturn201WithLocationHeader_whenDataIsValid() throws Exception {
            VolumeRequestDTO request = new VolumeRequestDTO(1, LocalDate.of(2024, 1, 1), new BigDecimal("39.90"), true);
            VolumeResponseDTO response = new VolumeResponseDTO(100L, 1, LocalDate.of(2024, 1, 1), new BigDecimal("39.90"), true, 10L);

            Mockito.when(service.create(eq(10L), any(VolumeRequestDTO.class), any()))
                    .thenReturn(response);

            mockMvc.perform(post("/works/10/volumes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/works/10/volumes/100")))
                    .andExpect(jsonPath("$.id", is(100)))
                    .andExpect(jsonPath("$.number", is(1)))
                    .andExpect(jsonPath("$.purchasePrice", is(39.90)))
                    .andExpect(jsonPath("$.workId", is(10)));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when volume number is not positive")
        void create_shouldReturn400_whenNumberIsNotPositive() throws Exception {
            VolumeRequestDTO request = new VolumeRequestDTO(0, LocalDate.now(), BigDecimal.TEN, true);

            mockMvc.perform(post("/works/10/volumes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("number")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when owned field is null")
        void create_shouldReturn400_whenOwnedIsNull() throws Exception {
            mockMvc.perform(post("/works/10/volumes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "number": 1,
                                      "purchasePrice": 20.0,
                                      "owned": null
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("owned")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when purchase price is negative")
        void create_shouldReturn400_whenPriceIsNegative() throws Exception {
            VolumeRequestDTO request = new VolumeRequestDTO(1, LocalDate.now(), new BigDecimal("-5.00"), true);

            mockMvc.perform(post("/works/10/volumes")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("purchasePrice")));
        }
    }

    @Nested
    @DisplayName("GET & DELETE volumes tests")
    class GetAndDeleteTests {

        @Test
        @DisplayName("Should return 200 OK with list of volumes for a work")
        void findAllByWorkId_shouldReturn200AndVolumeList() throws Exception {
            VolumeResponseDTO vol = new VolumeResponseDTO(100L, 1, LocalDate.now(), BigDecimal.TEN, true, 10L);
            Mockito.when(service.findAllByWorkId(eq(10L), any()))
                    .thenReturn(List.of(vol));

            mockMvc.perform(get("/works/10/volumes"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].id", is(100)))
                    .andExpect(jsonPath("$[0].number", is(1)));
        }

        @Test
        @DisplayName("Should return 200 OK with volume details when searching by ID")
        void findById_shouldReturn200AndVolume_whenExists() throws Exception {
            VolumeResponseDTO vol = new VolumeResponseDTO(100L, 1, LocalDate.now(), BigDecimal.TEN, true, 10L);
            Mockito.when(service.findById(eq(100L), any()))
                    .thenReturn(vol);

            mockMvc.perform(get("/volumes/100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id", is(100)))
                    .andExpect(jsonPath("$.number", is(1)));
        }

        @Test
        @DisplayName("Should return 204 No Content when deleting a volume")
        void delete_shouldReturn204_whenSuccessful() throws Exception {
            mockMvc.perform(delete("/volumes/100")
                            .with(csrf()))
                    .andExpect(status().isNoContent());

            Mockito.verify(service).delete(eq(100L), any());
        }
    }
}
