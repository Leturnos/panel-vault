package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.config.TokenService;
import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.WorkType;
import io.github.leturnos.panelvault.repository.UserRepository;
import io.github.leturnos.panelvault.service.WorkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkController.class)
@WithMockUser
class WorkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @DisplayName("Should return 404 ProblemDetail when work is not found by ID")
    void whenFindByIdNotFound_shouldReturn404AndProblemDetail() throws Exception {
        Mockito.when(service.findById(1L))
                .thenThrow(new ResourceNotFoundException("Obra não encontrada"));

        mockMvc.perform(get("/works/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title", is("Recurso Não Encontrado")))
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.detail", is("Obra não encontrada")))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    @DisplayName("Should return 400 with structured validation errors when request body is invalid")
    void whenCreateInvalidWork_shouldReturn400AndStructuredValidationErrors() throws Exception {
        mockMvc.perform(post("/works")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Falha na Validação")))
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.errors", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.errors[*].field", notNullValue()))
                .andExpect(jsonPath("$.errors[*].message", notNullValue()));
    }

    @Test
    @DisplayName("Should return 409 ProblemDetail when data integrity constraint is violated")
    void whenDataIntegrityViolated_shouldReturn409AndProblemDetail() throws Exception {
        Mockito.when(service.create(Mockito.any(WorkRequestDTO.class)))
                .thenThrow(new DataIntegrityViolationException("Erro de constraint no banco"));

        mockMvc.perform(post("/works")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Title",
                                  "type": "MANGA",
                                  "publisher": "Publisher",
                                  "author": "Author",
                                  "totalVolumes": 10,
                                  "coverUrl": "http://cover.jpg"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Violação de Integridade de Dados")))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.detail", containsString("restrição de integridade de dados")));
    }

    @Test
    @DisplayName("Should return 409 ProblemDetail with custom message on DuplicateResourceException")
    void whenDuplicateResource_shouldReturn409AndProblemDetailWithCustomMessage() throws Exception {
        Mockito.when(service.create(Mockito.any(WorkRequestDTO.class)))
                .thenThrow(new DuplicateResourceException("Já existe uma obra cadastrada com este título."));

        mockMvc.perform(post("/works")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Title",
                                  "type": "MANGA",
                                  "publisher": "Publisher",
                                  "author": "Author",
                                  "totalVolumes": 10,
                                  "coverUrl": "http://cover.jpg"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Conflito de Recursos")))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.detail", is("Já existe uma obra cadastrada com este título.")));
    }

    @Test
    @DisplayName("Should return 200 with paged works when retrieving all works without filter")
    void whenFindAll_shouldReturn200AndPagedWorks() throws Exception {
        WorkResponseDTO work = new WorkResponseDTO(
                1L, "Naruto", WorkType.MANGA, "Panini", "Kishimoto", 72, "http://cover.jpg"
        );
        Page<WorkResponseDTO> pagedWorks = new PageImpl<>(List.of(work));

        Mockito.when(service.findAll(Mockito.isNull(), Mockito.any(Pageable.class)))
                .thenReturn(pagedWorks);

        mockMvc.perform(get("/works"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Naruto")))
                .andExpect(jsonPath("$.content[0].publisher", is("Panini")));
    }

    @Test
    @DisplayName("Should return 200 with filtered paged works when title filter parameter is provided")
    void whenFindAllWithTitleFilter_shouldReturn200AndPagedFilteredWorks() throws Exception {
        WorkResponseDTO work = new WorkResponseDTO(
                1L, "Naruto", WorkType.MANGA, "Panini", "Kishimoto", 72, "http://cover.jpg"
        );
        Page<WorkResponseDTO> pagedWorks = new PageImpl<>(List.of(work));

        Mockito.when(service.findAll(Mockito.eq("Naruto"), Mockito.any(Pageable.class)))
                .thenReturn(pagedWorks);

        mockMvc.perform(get("/works").param("title", "Naruto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Naruto")));
    }
}
