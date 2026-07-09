package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
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
}
