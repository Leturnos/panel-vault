package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.service.WorkService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkController.class)
@WithMockUser
class WorkControllerExceptionTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkService service;

    @MockitoBean
    private io.github.leturnos.panelvault.config.TokenService tokenService;

    @MockitoBean
    private io.github.leturnos.panelvault.repository.UserRepository userRepository;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
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
    void whenCreateInvalidWork_shouldReturn400AndStructuredValidationErrors() throws Exception {
        // Envia um JSON vazio para disparar erros de validação
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
                                  "status": "ONGOING",
                                  "coverUrl": "http://cover.jpg"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Violação de Integridade de Dados")))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.detail", containsString("restrição de integridade de dados")));
    }

    @Test
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
                                  "status": "ONGOING",
                                  "coverUrl": "http://cover.jpg"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("Conflito de Recursos")))
                .andExpect(jsonPath("$.status", is(409)))
                .andExpect(jsonPath("$.detail", is("Já existe uma obra cadastrada com este título.")));
    }

    @Test
    void whenFindAll_shouldReturn200AndPagedWorks() throws Exception {
        io.github.leturnos.panelvault.dto.WorkResponseDTO work = new io.github.leturnos.panelvault.dto.WorkResponseDTO(
                1L, "Naruto", io.github.leturnos.panelvault.model.WorkType.MANGA, "Panini", "Kishimoto", 72, "http://cover.jpg"
        );
        org.springframework.data.domain.Page<io.github.leturnos.panelvault.dto.WorkResponseDTO> pagedWorks = new org.springframework.data.domain.PageImpl<>(java.util.List.of(work));

        Mockito.when(service.findAll(Mockito.isNull(), Mockito.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pagedWorks);

        mockMvc.perform(get("/works"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Naruto")))
                .andExpect(jsonPath("$.content[0].publisher", is("Panini")));
    }

    @Test
    void whenFindAllWithTitleFilter_shouldReturn200AndPagedFilteredWorks() throws Exception {
        io.github.leturnos.panelvault.dto.WorkResponseDTO work = new io.github.leturnos.panelvault.dto.WorkResponseDTO(
                1L, "Naruto", io.github.leturnos.panelvault.model.WorkType.MANGA, "Panini", "Kishimoto", 72, "http://cover.jpg"
        );
        org.springframework.data.domain.Page<io.github.leturnos.panelvault.dto.WorkResponseDTO> pagedWorks = new org.springframework.data.domain.PageImpl<>(java.util.List.of(work));

        Mockito.when(service.findAll(Mockito.eq("Naruto"), Mockito.any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(pagedWorks);

        mockMvc.perform(get("/works").param("title", "Naruto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Naruto")));
    }
}

